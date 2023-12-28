package com.tyron.code.desktop.ui.pane

import com.tyron.code.project.WorkspaceManager
import javafx.scene.layout.BorderPane


class WorkspaceRootPane(workspaceManager: WorkspaceManager) : BorderPane() {
    init {
        styleClass.add("bg-inset")
    }
}
