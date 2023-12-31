package com.tyron.code.desktop.ui.pane

import com.panemu.tiwulfx.control.dock.DetachableTab
import com.tyron.code.desktop.ui.control.FontIconView
import com.tyron.code.desktop.ui.control.richtext.Editor
import com.tyron.code.desktop.ui.control.richtext.source.CompletionProvider
import com.tyron.code.desktop.ui.docking.DockingManager
import com.tyron.code.desktop.ui.docking.DockingRegion
import com.tyron.code.java.analysis.Analyzer
import com.tyron.code.project.Workspace
import com.tyron.code.project.file.FileManager
import com.tyron.code.project.model.module.JavaModule
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import org.koin.java.KoinJavaComponent
import org.koin.mp.KoinPlatform.getKoin
import org.kordamp.ikonli.carbonicons.CarbonIcons
import java.util.concurrent.atomic.AtomicReference


class WorkspaceRootPane(private val dockingManager: DockingManager, workspace: Workspace) : BorderPane() {

    private val lastTreeRegion = AtomicReference<DockingRegion>()
    private val lastPrimaryRegion = AtomicReference<DockingRegion>()

    init {
        styleClass.add("bg-inset")

        println(workspace)
        workspace.addWorkspaceStateChangeListener {
            if (it == Workspace.State.INITIALIZED) {
                handleWorkspaceInitialized(workspace)
            }
        }
    }

    private fun handleWorkspaceInitialized(workspace: Workspace) {
        val dockTree: DockingRegion = dockingManager.newRegion()
        val dockPrimary: DockingRegion = dockingManager.primaryRegion

        createWorkspaceExplorerTab(dockTree, workspace);
        createPrimaryTab(dockPrimary, workspace);

        val split = SplitPane(dockTree, dockPrimary)
        SplitPane.setResizableWithParent(dockTree, false)
        split.setDividerPositions(0.333)
        center = split


        lastTreeRegion.set(dockTree);
        lastPrimaryRegion.set(dockPrimary);
    }

    private fun createWorkspaceExplorerTab(region: DockingRegion, workspace: Workspace) {
        val tab = region.createTab("Workspace", WorkspaceExplorerPane(workspace))
        tab.graphic = FontIconView(CarbonIcons.TREE_VIEW)
        tab.isClosable = false
        tab.isDetachable = false
    }

    private fun createPrimaryTab(region: DockingRegion, workspace: Workspace) {
        val infoTab: DetachableTab = region.createTab("Workspace Info", WorkspaceInformationPane(workspace))
        infoTab.graphic = FontIconView(CarbonIcons.INFORMATION)
    }
}