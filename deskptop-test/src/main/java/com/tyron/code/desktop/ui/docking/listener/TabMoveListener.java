package com.tyron.code.desktop.ui.docking.listener;

import com.tyron.code.desktop.ui.docking.DockingRegion;
import com.tyron.code.desktop.ui.docking.DockingTab;

public interface TabMoveListener {
    void onMove(DockingRegion oldRegion, DockingRegion newRegion, DockingTab tab);
}
