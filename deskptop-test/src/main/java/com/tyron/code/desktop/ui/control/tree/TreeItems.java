package com.tyron.code.desktop.ui.control.tree;

import com.tyron.code.project.util.Unchecked;
import org.jetbrains.annotations.NotNull;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Utilities for {@link TreeItem} types.
 *
 * @author Matt Coley
 */
public class TreeItems {
	/**
	 * Expand all parents to this item.
	 */
	public static void expandParents(@NotNull TreeItem<?> item) {
		while ((item = item.getParent()) != null)
			item.setExpanded(true);
	}

	/**
	 * Opens children recursively as long as only as there is only a path of single children.
	 *
	 * @param item
	 * 		Item to recursively open.
	 */
	public static void recurseOpen(@NotNull TreeItem<?> item) {
		item.setExpanded(true);
		if (item.getChildren().size() == 1)
			recurseOpen(item.getChildren().get(0));
	}

	/**
	 * Closes children recursively.
	 *
	 * @param tree
	 * 		Tree containing the item.
	 * @param item
	 * 		Item to recursively close.
	 */
	public static void recurseClose(@NotNull TreeView<?> tree, @NotNull TreeItem<?> item) {
		MultipleSelectionModel<TreeItem<?>> selectionModel = Unchecked.cast(tree.getSelectionModel());
		boolean wasSelected = selectionModel.getSelectedItem() == item;
		recurseClose(item);

		// For some reason closing a tree item screws with selection in weird ways.
		// So we'll re-select the item afterward if it was previously the selected item.
		if (wasSelected) selectionModel.select(item);
	}

	/**
	 * Closes children recursively.
	 *
	 * @param item
	 * 		Item to recursively close.
	 */
	private static void recurseClose(@NotNull TreeItem<?> item) {
		if (!item.isLeaf() && item.isExpanded()) {
			item.setExpanded(false);
			item.getChildren().forEach(TreeItems::recurseClose);
		}
	}
}