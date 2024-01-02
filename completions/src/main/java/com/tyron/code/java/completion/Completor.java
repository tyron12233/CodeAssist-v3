package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.Analyzer;
import com.tyron.code.java.parsing.FileContentFixer;
import com.tyron.code.java.parsing.Insertion;
import com.tyron.code.java.parsing.ParserContext;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.module.JavaModule;
import shadow.com.sun.source.tree.*;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.tools.javac.tree.JCTree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class Completor {

    private static final CompletionResult NO_CACHE =
            CompletionResult.builder()
                    .setFilePath(Paths.get(""))
                    .setLine(-1)
                    .setColumn(-1)
                    .setPrefix("")
                    .setCompletionCandidates(ImmutableList.of())
                    .setTextEditOptions(TextEditOptions.DEFAULT)
                    .build();

    private CompletionResult cachedCompletion = NO_CACHE;

    private final FileManager fileManager;

    private final Analyzer analyzer;

    public Completor(FileManager fileManager, Analyzer analyzer) {
        this.fileManager = fileManager;
        this.analyzer = analyzer;
    }

    public CompletionResult getCompletionResult(Path file, int line, int column) {
        // PositionContext gets the tree path whose leaf node includes the position
        // (position < node's endPosition). However, for completions, we want the leaf node either
        // includes the position, or just before the position (position == node's endPosition).
        // Decreasing column by 1 will decrease position by 1, which makes
        // adjustedPosition == node's endPosition - 1 if the node is just before the actual position.
        int contextColumn = column > 0 ? column - 1 : 0;

        ParserContext parserContext = new ParserContext();
        parserContext.setupLoggingSource(file.toString());
        FileContentFixer fileContentFixer = new FileContentFixer(parserContext);

        Optional<FileContentFixer.FixedContent> contents = fileManager.getFileContent(file)
                .map(fileContentFixer::fixFileContent);
        if (contents.isEmpty()) {
            return NO_CACHE;
        }

        LineMap adjustedLineMap = contents.get().getAdjustedLineMap();
        long offset = adjustedLineMap.getPosition(line + 1, column + 1);

        String adjustedContent = contents.get().getContent();
        char c = adjustedContent.charAt((int) offset - 1);
        if (!Character.isJavaIdentifierPart(c) && c != '.') {
            // append dummy identifier so that we can complete in this context
            Insertion insertion = Insertion.create((int) offset, "dumbIdent");
            adjustedContent = Insertion.applyInsertions(adjustedContent, List.of(insertion)).toString();

            adjustedLineMap = FileContentFixer.createAdjustedLineMap(
                    adjustedLineMap,
                    List.of(insertion)
            );

            offset = adjustedLineMap.getPosition(line + 1, column + 1);
        }


        ContentWithLineMap contentWithLineMap = ContentWithLineMap.create(adjustedContent, adjustedLineMap, file);
        String prefix = contentWithLineMap.extractCompletionPrefix((int) offset);

        System.out.println(prefix);

        // TODO: limit the number of the candidates.
        if (cachedCompletion.isIncrementalCompletion(file, line, column, prefix)) {
            return getCompletionCandidatesFromCache(line, column, prefix);
        } else {
            cachedCompletion =
                    computeCompletionResult(
                            file,
                            contentWithLineMap.getContent().toString(),
                            line,
                            column,
                            ((int) offset),
                            prefix
                    );
            return cachedCompletion;
        }
    }

    private CompletionResult computeCompletionResult(
            Path file,
            String fixedContents,
            int line,
            int column,
            int offset,
            String prefix) {
        TextEditOptions.Builder textEditOptions =
                TextEditOptions.builder().setAppendMethodArgumentSnippets(false);

        CompletableFuture<ImmutableList<CompletionCandidate>> future = new CompletableFuture<>();

        try {
            analyzer.analyze(file, fixedContents, analysisResult -> {
                JavaModule module = analysisResult.module();
                JCTree.JCCompilationUnit jcCompilationUnit = (JCTree.JCCompilationUnit) analysisResult.parsedTree();
                analyzer.checkCancelled();

                FindCompletionsAt findCompletionsAt = new FindCompletionsAt(analysisResult.javacTask());
                TreePath currentAnalyzedPath = findCompletionsAt.scan(jcCompilationUnit, (long) offset);
                CompletionArgs args = new CompletionArgs(null, module, currentAnalyzedPath, analysisResult, prefix);
                analyzer.checkCancelled();

                CompletionAction action = getCompletionAction(currentAnalyzedPath);

                if (action == null) {
                    future.complete(ImmutableList.of());
                    return;
                }


                try {
                    ImmutableList<CompletionCandidate> candidates = action.getCompletionCandidates(args);
                    future.complete(candidates);
                } catch (CancellationException e) {
                    future.complete(ImmutableList.of());
                }
            });
        } catch (CancellationException e) {
            future.complete(ImmutableList.of());
        }


        ImmutableList<CompletionCandidate> candidates =
                future.join();
        return CompletionResult.builder()
                .setFilePath(file)
                .setLine(line)
                .setColumn(column)
                .setPrefix(prefix)
                .setCompletionCandidates(candidates)
                .setTextEditOptions(textEditOptions.build())
                .build();
    }

    private static CompletionAction getCompletionAction(TreePath currentAnalyzedPath) {
        CompletionAction action;
        if (currentAnalyzedPath.getLeaf() instanceof MemberSelectTree || currentAnalyzedPath.getLeaf() instanceof ImportTree importTree) {
            action = new CompleteMemberSelectAction();
        } else if (currentAnalyzedPath.getLeaf() instanceof IdentifierTree) {
            action = new CompleteSymbolAction();
        } else if (currentAnalyzedPath.getLeaf() instanceof LiteralTree)
            action = NoCandidateAction.INSTANCE;
        else {
            action = null;
        }
        return action;
    }

    private CompletionResult getCompletionCandidatesFromCache(int line, int column, String prefix) {

        ImmutableList<CompletionCandidate> narrowedCandidates =
                new CompletionCandidateListBuilder(prefix)
                        .addCandidates(cachedCompletion.getCompletionCandidates())
                        .build();
        return cachedCompletion
                .toBuilder()
                .setCompletionCandidates(narrowedCandidates)
                .setLine(line)
                .setColumn(column)
                .setPrefix(prefix)
                .build();
    }

    private static <T extends Tree> Optional<T> findNodeOfType(TreePath treePath, Class<T> type) {
        while (treePath != null) {
            Tree leaf = treePath.getLeaf();
            if (type.isAssignableFrom(leaf.getClass())) {
                @SuppressWarnings("unchecked")
                T casted = (T) leaf;
                return Optional.of(casted);
            }
            treePath = treePath.getParentPath();
        }
        return Optional.empty();
    }

    private static class NoCandidateAction implements CompletionAction {
        public static final NoCandidateAction INSTANCE = new NoCandidateAction();

        @Override
        public ImmutableList<CompletionCandidate> getCompletionCandidates(CompletionArgs args) {
            return ImmutableList.of();
        }
    }
}
