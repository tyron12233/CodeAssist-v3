package com.tyron.code.desktop.ui.control.richtext.syntax;

import org.jetbrains.annotations.NotNull;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

/**
 * Wrapper of created style-spans and starting position.
 *
 * @author Matt Coley
 */
public record StyleResult(@NotNull StyleSpans<Collection<String>> spans, int position) {
}
