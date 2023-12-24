package com.tyron.code.java.completion;

import com.google.auto.value.AutoValue;
import com.tyron.code.java.parsing.FileContentFixer;
import com.tyron.code.java.parsing.LineMapUtil;
import com.tyron.code.java.parsing.PositionContext;
import shadow.com.sun.source.tree.LineMap;
import shadow.com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Combines file content with line map for easier lookup.
 *
 * <p>The content and line map are from the original content, i.e. not modified by {@link
 * FileContentFixer}.
 */
@AutoValue
abstract class ContentWithLineMap {
    public static ContentWithLineMap create(String content, LineMap lineMap, Path path) {
        return new AutoValue_ContentWithLineMap(
                content,
                lineMap,
                path
        );
    }

//    private static final Logger logger = Logger.getLogger("main");

    abstract CharSequence getContent();

    abstract LineMap getLineMap();

    abstract Path getFilePath();

    /** Gets the content before cursor position (line, column) as prefix for completion. */
    String extractCompletionPrefix(int position) {
        int start = position - 1;
        while (start >= 0 && Character.isJavaIdentifierPart(getContent().charAt(start))) {
            start--;
        }
        return getContent().subSequence(start + 1, position).toString();
    }

    String substring(int line, int column, int length) {
        int position = LineMapUtil.getPositionFromZeroBasedLineAndColumn(getLineMap(), line, column);
//        if (position < 0) {
//            logger.warning(
//                    "Position of (%s, %s): %s is negative when getting substring for file %s",
//                    line, column, position, getFilePath());
//            return "";
//        }
        CharSequence content = getContent();
//        if (content.length() < position) {
//            logger.warning(
//                    "Position of (%s, %s): %s is greater than the length of the content %s when "
//                            + "getting substring for file %s",
//                    line, column, position, content.length(), getFilePath());
//            return "";
//        }
        return content.subSequence(position, Math.min(content.length(), position + length)).toString();
    }
}