package com.tyron.code.desktop.ui.control.richtext;

import org.jetbrains.annotations.NotNull;

/**
 * Outline for all components that can be added to an {@link Editor}.
 *
 * @author Matt Coley
 */
public interface EditorComponent {
	/**
	 * Called when the component is installed into the given editor.
	 *
	 * @param editor
	 * 		Editor installed to.
	 */
	void install(@NotNull Editor editor);

	/**
	 * Called when the component is removed from the given editor.
	 *
	 * @param editor
	 * 		Editor removed from.
	 */
	void uninstall(@NotNull Editor editor);
}
