package com.tyron.code.desktop

import com.tyron.code.desktop.services.navigation.NavigationManager
import com.tyron.code.desktop.ui.control.FontIconView
import com.tyron.code.desktop.ui.docking.DockingManager
import com.tyron.code.desktop.ui.docking.DockingRegion
import com.tyron.code.desktop.ui.pane.LoggingPane
import com.tyron.code.desktop.ui.pane.WelcomePane
import com.tyron.code.desktop.ui.pane.WorkspaceRootPane
import com.tyron.code.desktop.util.FxThreadUtils
import com.tyron.code.logging.Logging
import com.tyron.code.project.*
import com.tyron.code.project.impl.WorkspaceImpl
import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.koin.core.component.getScopeId
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.java.KoinJavaComponent.inject
import org.kordamp.ikonli.carbonicons.CarbonIcons
import org.slf4j.event.Level
import java.nio.file.Paths


class CodeAssistApplication : Application(), WorkspaceCloseListener, WorkspaceOpenListener {

    private val logger = Logging.get(CodeAssistApplication::class.java)

    private val workspaceManager: WorkspaceManager by inject(WorkspaceManager::class.java)
    private val root = BorderPane()
    private val welcomePane: WelcomePane by inject(WelcomePane::class.java)
    private val workspaceRootPane: WorkspaceRootPane by inject(WorkspaceRootPane::class.java)
    override fun start(stage: Stage) {
        setUserAgentStylesheet("/style/codeassist.css")

        Logging.setInterceptLevel(Level.DEBUG)

        val logging = createLoggingWrapper()
        val splitPane = SplitPane(root, logging)
        SplitPane.setResizableWithParent(logging, false)
        splitPane.orientation = Orientation.VERTICAL
        splitPane.setDividerPositions(0.21)
        val wrapper = BorderPane()
        wrapper.center = splitPane
        wrapper.styleClass.addAll("padded", "bg-inset")
        root.center = welcomePane
        workspaceManager.addWorkspaceOpenListener(this)
        workspaceManager.addWorkspaceCloseListener(this)

        val scene = Scene(wrapper)
        stage.apply {
            minWidth = 900.0
            minHeight = 600.0
            setScene(scene)
            title = "CodeAssist"
        }
        stage.show()

        eagerInit()

        testInit()
    }

    private fun eagerInit() {
        getKoin().get<NavigationManager>()
    }

    private fun testInit() {
        val root = Paths.get("deskptop-test/src/test/resources/TestProject")

        logger.debug("Initializing workspace at {}", root)

        val workspace = WorkspaceImpl(root)
        workspaceManager.current = workspace

        workspace.state = Workspace.State.INITIALIZING
        val workspaceScope = getKoin().getOrCreateScope<Workspace>(workspace.getScopeId())
        val moduleManager = workspaceScope.get<ModuleManager>()
        moduleManager.initialize()

        workspace.setRoot(moduleManager.rootModule)
        workspace.state = Workspace.State.INITIALIZED
        logger.debug("Workspace initialized")
    }

    private fun createLoggingWrapper(): Node {
        val logging = LoggingPane()
        val dockingPane: DockingRegion = getKoin().get<DockingManager>().newRegion()
        val tab = dockingPane.createTab("Logging", logging)
        tab.graphic = FontIconView(CarbonIcons.TERMINAL)
        tab.isClosable = false
        return dockingPane
    }

    override fun onWorkspaceClosed(workspace: Workspace) {
        FxThreadUtils.run { root.center = welcomePane }
    }

    override fun onWorkspaceOpened(workspace: Workspace) {
        FxThreadUtils.run { root.center = workspaceRootPane }
    }
}
