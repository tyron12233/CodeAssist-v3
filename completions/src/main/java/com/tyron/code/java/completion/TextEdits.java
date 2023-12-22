package com.tyron.code.java.completion;

import com.google.common.base.Joiner;
import com.tyron.code.java.protocol.Position;
import com.tyron.code.java.protocol.Range;
import shadow.com.sun.source.tree.CompilationUnitTree;
import shadow.com.sun.source.tree.IdentifierTree;
import shadow.com.sun.source.tree.ImportTree;
import shadow.com.sun.source.tree.LineMap;
import shadow.com.sun.source.tree.MemberSelectTree;
import shadow.com.sun.source.tree.Tree;
import shadow.com.sun.source.util.TreeScanner;
import shadow.com.sun.tools.javac.tree.EndPosTable;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/** Generates text edits for completion items. */
public class TextEdits {
    private static final Joiner QUALIFIER_JOINER = Joiner.on(".");
    private static final long INVALID_POS = -1;



    private static Range createRange(long pos, LineMap lineMap) {
        Position position =
                new Position((int) lineMap.getLineNumber(pos) - 1, (int) lineMap.getColumnNumber(pos) - 1);
        return new Range(position, position);
    }

    private static class ImportClassScanner extends TreeScanner<Void, Void> {
        private final String fullClassName;
        private final LineMap lineMap;
        private final EndPosTable endPosTable;

        private long afterPackagePos = INVALID_POS;
        private long beforeImportPos = INVALID_POS;
        private long afterImportPos = INVALID_POS;
        private long afterStaticImportsPos = INVALID_POS;
        private boolean isImported;

        private ImportClassScanner(String fullClassName, LineMap lineMap, EndPosTable endPosTable) {
            this.fullClassName = fullClassName;
            this.lineMap = lineMap;
            this.endPosTable = endPosTable;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void unused) {
            if (node.getPackageName() != null) {
                // It's weird that package end pos doesn't contain the ending semicolon.
                afterPackagePos = endPosTable.getEndPos((JCTree) node.getPackageName()) + 1;
            }

            if (node.getImports() != null) {
                for (ImportTree importTree : node.getImports()) {
                    scan(importTree, null);
                }
            }
            return null;
        }

        @Override
        public Void visitImport(ImportTree node, Void unused) {
            if (node.isStatic()) {
                afterStaticImportsPos = endPosTable.getEndPos((JCTree) node);
                return null;
            }

            String importedName = nameTreeToQualifiedName(node.getQualifiedIdentifier());
            int cmp = importedName.compareTo(fullClassName);
            if (cmp < 0) {
                // The existing import statement should be above the new one.
                afterImportPos = endPosTable.getEndPos((JCTree) node);
            } else if (cmp > 0) {
                // The new import statement should be above the existing one, and also
                // previous existing ones.
                if (beforeImportPos == INVALID_POS) {
                    beforeImportPos = ((JCTree) node).getStartPosition();
                }
            } else {
                isImported = true;
            }

            return null;
        }

        private static String nameTreeToQualifiedName(Tree name) {
            Deque<String> stack = new ArrayDeque<>();
            while (name instanceof MemberSelectTree) {
                MemberSelectTree qualifiedName = (MemberSelectTree) name;
                stack.addFirst(qualifiedName.getIdentifier().toString());
                name = qualifiedName.getExpression();
            }
            stack.addFirst(((IdentifierTree) name).getName().toString());
            return QUALIFIER_JOINER.join(stack);
        }
    }
}