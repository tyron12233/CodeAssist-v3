package com.tyron.code.desktop.ui.control.richtext;

import org.jetbrains.annotations.NotNull;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Used by {@link Node} values to be shown on an editor's {@link Editor#getPrimaryStack()}.
 * If a node is shown on the right, the padding will need to be adjusted when scrollbars are visible.
 *
 * @author Matt Coley
 * @see ProblemOverlay
 * @see AbstractDecompilerPaneConfigurator
 */
public class ScrollbarPaddingUtil {
	/**
	 * When the {@link Editor#getVerticalScrollbar()} is visible, our {@link StackPane#setMargin(Node, Insets)} will cause
	 * us to overlap with it. This doesn't look great, so when it is visible we will shift a bit over to the left so that we
	 * do not overlap.
	 *
	 * @param node
	 * 		Node to update.
	 * @param currentlyVisible
	 * 		Current visibility state of the editor's vertical scrollbar.
	 */
	public static void handleScrollbarVisibility(@NotNull Node node, boolean currentlyVisible) {
		StackPane.setMargin(node, new Insets(7, currentlyVisible ? 14 : 7, 7, 7));
	}
}
