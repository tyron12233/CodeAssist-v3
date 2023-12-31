package com.tyron.code.desktop.ui.docking.listener;

import com.tyron.code.desktop.ui.docking.DockingRegion;
import com.tyron.code.desktop.ui.docking.DockingTab;

public interface TabCreationListener {
    void onCreate(DockingRegion parent, DockingTab tab);
}
