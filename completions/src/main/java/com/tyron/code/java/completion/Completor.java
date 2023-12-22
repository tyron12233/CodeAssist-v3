package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.Analyzer;
import com.tyron.code.java.parsing.PositionContext;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.ProjectModule;
import shadow.com.sun.source.tree.*;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.tools.javac.tree.JCTree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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

    public Completor(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public CompletionResult getCompletionResult(ProjectModule module, Path file, int line, int column) {
        // PositionContext gets the tree path whose leaf node includes the position
        // (position < node's endPosition). However, for completions, we want the leaf node either
        // includes the position, or just before the position (position == node's endPosition).
        // Decreasing column by 1 will decrease position by 1, which makes
        // adjustedPosition == node's endPosition - 1 if the node is just before the actual position.
        int contextColumn = column > 0 ? column - 1 : 0;
        Optional<PositionContext> positionContext =
                PositionContext.createForPosition(fileManager, module, file, line, contextColumn);
        if (positionContext.isEmpty()) {
            return CompletionResult.builder()
                    .setCompletionCandidates(ImmutableList.of())
                    .setLine(line)
                    .setColumn(column)
                    .setPrefix("")
                    .setFilePath(file)
                    .build();
        }

        ContentWithLineMap contentWithLineMap =
                ContentWithLineMap.create(positionContext.get());
        String prefix = contentWithLineMap.extractCompletionPrefix(line, column);
        // TODO: limit the number of the candidates.
        if (cachedCompletion.isIncrementalCompletion(file, line, column, prefix)) {
            return getCompletionCandidatesFromCache(line, column, prefix);
        } else {
            cachedCompletion =
                    computeCompletionResult(positionContext.get(), contentWithLineMap, line, column, prefix);
            return cachedCompletion;
        }
    }

    private CompletionResult computeCompletionResult(
            PositionContext positionContext,
            ContentWithLineMap contentWithLineMap,
            int line,
            int column,
            String prefix) {
        TreePath treePath = positionContext.getTreePath();
        CompletionAction action;
        TextEditOptions.Builder textEditOptions =
                TextEditOptions.builder().setAppendMethodArgumentSnippets(false);

        if (treePath.getLeaf() instanceof MemberSelectTree) {
            ExpressionTree parentExpression = ((MemberSelectTree) treePath.getLeaf()).getExpression();
            Optional<ImportTree> importNode = findNodeOfType(treePath, ImportTree.class);
            if (importNode.isPresent()) {
//                if (importNode.get().isStatic()) {
//                    action =
//                            CompleteMemberAction.forImportStatic(parentExpression, typeSolver, expressionSolver);
//                } else {
//                    action = CompleteMemberAction.forImport(parentExpression, typeSolver, expressionSolver);
//                }
                action = null;

            } else {
                action = new CompleteMemberSelectAction();
                textEditOptions.setAppendMethodArgumentSnippets(true);
            }
        } else if (treePath.getLeaf() instanceof IdentifierTree) {
            action = new CompleteSymbolAction();
        } else {
            action = null;
        }
        if (action == null) {
            return NO_CACHE;
        }

        // When the cursor is before an opening parenthesis, it's likely the user is
        // trying to change the name of a method invocation. In this case the
        // arguments are already there, and we should not append method argument
        // snippet upon completion.
        if ("(".equals(contentWithLineMap.substring(line, column, 1))) {
            textEditOptions.setAppendMethodArgumentSnippets(false);
        }

        CompletableFuture<ImmutableList<CompletionCandidate>> future = new CompletableFuture<>();

        Analyzer analyzer = new Analyzer();
        analyzer.analyze(positionContext.getPath(), positionContext.getContent().toString(), fileManager, positionContext.getModule(), analysisResult -> {
            JCTree.JCCompilationUnit jcCompilationUnit = (JCTree.JCCompilationUnit) analysisResult.parsedTree();
            PositionContext.PositionAstScanner scanner = new PositionContext.PositionAstScanner(jcCompilationUnit.endPositions, positionContext.getPosition());
            TreePath currentAnalyzedPath = scanner.scan(analysisResult.parsedTree(), null);
            CompletionArgs args = new CompletionArgs(positionContext, currentAnalyzedPath, analysisResult, prefix);
            ImmutableList<CompletionCandidate> candidates = action.getCompletionCandidates(args);
            future.complete(candidates);
        });


        ImmutableList<CompletionCandidate> candidates =
                future.join();
        return CompletionResult.builder()
                .setFilePath(contentWithLineMap.getFilePath())
                .setLine(line)
                .setColumn(column)
                .setPrefix(prefix)
                .setCompletionCandidates(candidates)
                .setTextEditOptions(textEditOptions.build())
                .build();
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

}
