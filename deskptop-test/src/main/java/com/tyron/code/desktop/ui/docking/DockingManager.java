package com.tyron.code.desktop.ui.docking;

import com.tyron.code.desktop.ui.docking.listener.TabClosureListener;
import com.tyron.code.desktop.ui.docking.listener.TabCreationListener;
import com.tyron.code.desktop.ui.docking.listener.TabMoveListener;
import com.tyron.code.desktop.ui.docking.listener.TabSelectionListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Manages open docking regions and tabs.
 *
 * @author Matt Coley
 */
public class DockingManager {
	private final DockingRegionFactory factory = new DockingRegionFactory(this);
	private final DockingRegion primaryRegion;
	private final List<DockingRegion> regions = new ArrayList<>();
	private final List<TabSelectionListener> tabSelectionListeners = new ArrayList<>();
	private final List<TabCreationListener> tabCreationListeners = new ArrayList<>();
	private final List<TabClosureListener> tabClosureListeners = new ArrayList<>();
	private final List<TabMoveListener> tabMoveListeners = new ArrayList<>();

	public DockingManager() {
		primaryRegion = newRegion();
		primaryRegion.setCloseIfEmpty(false);
	}

	/**
	 * The primary region is where tabs should open by default.
	 * It is locked to the main window where the initial workspace info is displayed.
	 * Compared to an IDE, this would occupy the same space where open classes are shown.
	 *
	 * @return Primary region.
	 */
	@NotNull
	public DockingRegion getPrimaryRegion() {
		return primaryRegion;
	}

	/**
	 * @return All current docking regions.
	 */
	@NotNull
	public List<DockingRegion> getRegions() {
		return regions;
	}

	/**
	 * @return All current docking tabs.
	 */
	@NotNull
	public List<DockingTab> getDockTabs() {
		return regions.stream().flatMap(r -> r.getDockTabs().stream()).toList();
	}

	/**
	 * @return New region.
	 */
	@NotNull
	public DockingRegion newRegion() {
		DockingRegion region = factory.create();
		factory.init(region);
		regions.add(region);
		return region;
	}

	/**
	 * Configured by {@link DockingRegionFactory} this method is called when a {@link DockingRegion} is closed.
	 *
	 * @param region
	 * 		Region being closed.
	 *
	 * @return {@code true} when region closure was a success.
	 * {@code false} when a region denied closure.
	 */
	boolean onRegionClose(@NotNull DockingRegion region) {
		// Close any tabs that are closable.
		// If there are tabs that cannot be closed, deny region closure.
		boolean allowClosure = true;
		for (DockingTab tab : new ArrayList<>(region.getDockTabs()))
			if (tab.isClosable())
				tab.close();
			else
				allowClosure = false;
		if (!allowClosure)
			return false;

		// Update internal state.
		regions.remove(region);

		// Needed in case a window containing the region gets closed.
		for (DockingTab tab : new ArrayList<>(region.getDockTabs()))
			tab.close();

		// Tell the region it is closed, removing its reference to this docking manager.
		region.onClose();

		// Closure allowed.
		return true;
	}

	/**
	 * Configured by {@link DockingRegion#createTab(Supplier)}, called when a tab is created.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabCreate(@NotNull DockingRegion parent, @NotNull DockingTab tab) {
		for (TabCreationListener listener : tabCreationListeners) {
            listener.onCreate(parent, tab);
        }
	}

	/**
	 * Configured by {@link DockingRegion#createTab(Supplier)}, called when a tab is closed.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabClose(@NotNull DockingRegion parent, @NotNull DockingTab tab) {
		for (TabClosureListener listener : tabClosureListeners) {
            listener.onClose(parent, tab);
        }
	}

	/**
	 * Configured by {@link DockingRegion#createTab(Supplier)}, called when a tab is
	 * moved between {@link DockingRegion}s.
	 *
	 * @param oldRegion
	 * 		Prior parent region.
	 * @param newRegion
	 * 		New parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabMove(@NotNull DockingRegion oldRegion, @NotNull DockingRegion newRegion, @NotNull DockingTab tab) {
		for (TabMoveListener listener : tabMoveListeners) {
            listener.onMove(oldRegion, newRegion, tab);
        }
	}

	/**
	 * Configured by {@link DockingRegion#createTab(Supplier)}, called when a tab is selected.
	 *
	 * @param parent
	 * 		Parent region.
	 * @param tab
	 * 		Tab created.
	 */
	void onTabSelection(@NotNull DockingRegion parent, @NotNull DockingTab tab) {
		for (TabSelectionListener listener : tabSelectionListeners) {
            listener.onSelection(parent, tab);
        }
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabSelectionListener(@NotNull TabSelectionListener listener) {
		tabSelectionListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabSelectionListener(@NotNull TabSelectionListener listener) {
		return tabSelectionListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabCreationListener(@NotNull TabCreationListener listener) {
		tabCreationListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabCreationListener(@NotNull TabCreationListener listener) {
		return tabCreationListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabClosureListener(@NotNull TabClosureListener listener) {
		tabClosureListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabClosureListener(@NotNull TabClosureListener listener) {
		return tabClosureListeners.remove(listener);
	}

	/**
	 * @param listener
	 * 		Listener to add.
	 */
	public void addTabMoveListener(@NotNull TabMoveListener listener) {
		tabMoveListeners.add(listener);
	}

	/**
	 * @param listener
	 * 		Listener to remove.
	 *
	 * @return {@code true} upon removal. {@code false} when listener wasn't present.
	 */
	public boolean removeTabMoveListener(@NotNull TabMoveListener listener) {
		return tabMoveListeners.remove(listener);
	}
}