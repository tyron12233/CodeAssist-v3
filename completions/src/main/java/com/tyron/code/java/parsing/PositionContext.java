package com.tyron.code.java.parsing;

import com.google.auto.value.AutoValue;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import shadow.com.sun.source.tree.ErroneousTree;
import shadow.com.sun.source.tree.LineMap;
import shadow.com.sun.source.tree.Tree;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.TreePathScanner;
import shadow.com.sun.tools.javac.tree.EndPosTable;
import shadow.com.sun.tools.javac.tree.JCTree;

import java.nio.file.Path;
import java.util.Optional;

@AutoValue
public abstract class PositionContext {

    public abstract Module getModule();

    public abstract Path getPath();

    public abstract TreePath getTreePath();

    /**
     * Gets position of the parsed content of the file.
     *
     * <p>Because the content of a file may be modified by {@link FileContentFixer}, the parsed
     * content may be different from the original content of the file. So this position can not be
     * used in the context of the original position.
     */
    public abstract long getPosition();

    public abstract EndPosTable getEndPosTable();

    public abstract CharSequence getContent();

    /**
     * Creates a {@link PositionContext} instance based on the given file path and position.
     *
     * @param fileManager
     * @param module      the module of the project
     * @param filePath    normalized path of the file to be completed
     * @param line        0-based line number of the completion point
     * @param column      0-based character offset from the beginning of the line to the completion point
     */
    public static Optional<PositionContext> createForPosition(
            FileManager fileManager, JavaModule module, Path filePath, int line, int column) {
        Optional<JavaFileInfo> inputFileScope = module.getFile(filePath.toString());
        if (inputFileScope.isEmpty()) {
            return Optional.empty();
        }

        String contents = fileManager.getFileContent(filePath).orElseThrow().toString();



        ParserContext context = new ParserContext();
        FileContentFixer fileContentFixer = new FileContentFixer(context);
        FileContentFixer.FixedContent fixedContent = fileContentFixer.fixFileContent(contents);

        JCTree.JCCompilationUnit unit = context.parse(filePath.toString(), fixedContent.getContent());

        LineMap lineMap = fixedContent.getAdjustedLineMap();
        int position = LineMapUtil.getPositionFromZeroBasedLineAndColumn(lineMap, line, column);

        PositionAstScanner scanner = new PositionAstScanner(unit.endPositions, position);
        TreePath currentPath = scanner.scan(unit, null);
        return Optional.of(new AutoValue_PositionContext(module, filePath, currentPath, position, unit.endPositions, fixedContent.getContent()));
    }

    /** A {@link TreePathScanner} that returns the tree path enclosing the given position. */
    public static class PositionAstScanner extends TreePathScanner<TreePath, Void> {
        private final EndPosTable endPosTable;
        private final int position;

        public PositionAstScanner(EndPosTable endPosTable, int position) {
            this.endPosTable = endPosTable;
            this.position = position;
        }

        @Override
        public TreePath scan(Tree tree, Void unused) {
            if (tree == null) {
                return null;
            }

            JCTree jcTree = (JCTree) tree;
            int startPosition = jcTree.getStartPosition();
            int endPosition = jcTree.getEndPosition(endPosTable);
            boolean positionInNodeRange =
                    (startPosition < 0 || startPosition <= position)
                            && (position < endPosition || endPosition < 0);
//            logger.fine(
//                    "PositionAstScanner: visiting node: %s, start: %s, end: %s.%s",
//                    tree.accept(new TreePathFormatter.TreeFormattingVisitor(), null),
//                    jcTree.getStartPosition(),
//                    jcTree.getEndPosition(endPosTable),
//                    positionInNodeRange ? " ✔" : "");
            if (!positionInNodeRange) {
                return null;
            }
            TreePath currentPath = new TreePath(getCurrentPath(), tree);

            TreePath ret = super.scan(tree, null);
            if (ret != null) {
                return ret;
            }

            if (tree instanceof ErroneousTree erroneousTree) {
                System.out.println(erroneousTree);
                return null;
            }
            return currentPath;
        }

        @Override
        public TreePath visitErroneous(ErroneousTree node, Void unused) {
            for (Tree tree : node.getErrorTrees()) {
                TreePath ret = scan(tree, unused);
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }

        @Override
        public TreePath reduce(TreePath r1, TreePath r2) {
            if (r1 != null) {
                return r1;
            }
            return r2;
        }
    }
}
