package com.tyron.code.desktop

import atlantafx.base.theme.PrimerLight
import com.tyron.code.desktop.ui.pane.WelcomePane
import com.tyron.code.desktop.ui.pane.WorkspaceRootPane
import com.tyron.code.project.Workspace
import com.tyron.code.project.WorkspaceCloseListener
import com.tyron.code.project.WorkspaceManager
import com.tyron.code.project.WorkspaceOpenListener
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.stage.Stage
import org.koin.java.KoinJavaComponent.inject


class CodeAssistApplication : Application(), WorkspaceCloseListener, WorkspaceOpenListener {
    private val workspaceManager : WorkspaceManager by inject(WorkspaceManager::class.java)
    private val root = BorderPane()
    private val welcomePane = WelcomePane()
    private val workspaceRootPane : WorkspaceRootPane by inject(WorkspaceRootPane::class.java)
    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)
        val logging = BorderPane(Text("Logs"))
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
    }

    override fun onWorkspaceClosed(workspace: Workspace) {
        Platform.runLater { root.center = welcomePane }
    }

    override fun onWorkspaceOpened(workspace: Workspace) {
        println("OPENED")
        Platform.runLater { root.center = workspaceRootPane }
    }
}
