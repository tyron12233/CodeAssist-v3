package com.tyron.code.desktop.ui

import com.tyron.code.desktop.CodeAssistApplication
import com.tyron.code.desktop.cdi.WorkspaceImpl
import com.tyron.code.desktop.cdi.mainModule
import com.tyron.code.project.ModuleManager
import com.tyron.code.project.Workspace
import com.tyron.code.project.WorkspaceManager
import com.tyron.code.project.file.FileManager
import javafx.application.Application
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.scope.get
import org.koin.java.KoinJavaComponent.get
import java.nio.file.Paths

fun main() {

    startKoin {
        modules(mainModule)
    }

    var workspace = WorkspaceImpl(Paths.get("/non-existent"))
    val workspaceManager : WorkspaceManager = get(WorkspaceManager::class.java)
    workspaceManager.current = workspace

    workspace = get(Workspace::class.java)

    val moduleManager = workspace.get<ModuleManager>()
    moduleManager.initialize()

    Application.launch(CodeAssistApplication::class.java)
}
