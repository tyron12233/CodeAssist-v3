package com.tyron.code.desktop.ui.control.richtext.syntax;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.EditorComponent;
import com.tyron.code.desktop.util.IntRange;
import org.jetbrains.annotations.NotNull;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;


import java.util.Collection;

/**
 * Outline of basic syntax highlighting.
 *
 * @author Matt Coley
 * @see RegexSyntaxHighlighter Regex implementation.
 */
public interface SyntaxHighlighter extends EditorComponent {
	/**
	 * @param text
	 * 		Full text.
	 * @param start
	 * 		Start range in text to style.
	 * @param end
	 * 		End range in text to style.
	 *
	 * @return Spans for RichTextFX.
	 */
	@NotNull
	StyleSpans<Collection<String>> createStyleSpans(@NotNull String text, int start, int end);

	/**
	 * By default, the {@link Editor} will create a base range to restyle when a text change is made.
	 * However, in some cases that range may not be complete due to the change made.
	 * <br>
	 * Consider in a multi-line comment if you remove the last {@code '/'}. The end is now the next {@code '*\/'} found
	 * in the document, which can be much further along in the text than the range created by the {@link Editor}.
	 * Thus, implementations of {@link SyntaxHighlighter} are given the ability to expand ranges when cases like this
	 * exist.
	 *
	 * @param text
	 * 		Full text.
	 * @param initialStart
	 * 		Start range in text to style.
	 * @param initialEnd
	 * 		End range in text to style.
	 *
	 * @return Expanded range to style.
	 * By default, returns the given range.
	 *
	 * @see SyntaxUtil#getRangeForRestyle(PlainTextChange, Editor) Usage of this method.
	 */
	@NotNull
	default IntRange expandRange(@NotNull String text, int initialStart, int initialEnd) {
		return new IntRange(initialStart, initialEnd);
	}

	/**
	 * Called when the syntax highlighter is {@link Editor#setSyntaxHighlighter(SyntaxHighlighter) installed} into
	 * the given editor.
	 * <br>
	 * Something a syntax highlighter may want to do is install a custom stylesheet via {@link Editor#getStylesheets()}.
	 *
	 * @param editor
	 * 		Editor installed to.
	 */
	@Override
	default void install(@NotNull Editor editor) {
		// no-op by default
	}

	/**
	 * Called when the syntax highlighter is removed from the given editor.
	 * <br>
	 * Should clean up any actions done in {@link #install(Editor)}.
	 *
	 * @param editor
	 * 		Editor removed from.
	 */
	@Override
	default void uninstall(@NotNull Editor editor) {
		// no-op by default
	}
}
