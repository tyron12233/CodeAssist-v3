package com.tyron.code.desktop.ui.docking.listener;

import com.tyron.code.desktop.ui.docking.DockingRegion;
import com.tyron.code.desktop.ui.docking.DockingTab;

/**
 * Listener for {@link DockingTab} being closed.
 *
 * @author Matt Coley
 */
public interface TabClosureListener {
	/**
	 * @param parent
	 * 		Parent region the tab belonged to.
	 * @param tab
	 * 		Tab closed.
	 */
	void onClose(DockingRegion parent, DockingTab tab);
}