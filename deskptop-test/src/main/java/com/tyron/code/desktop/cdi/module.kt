package com.tyron.code.desktop.cdi

import com.tyron.code.desktop.ui.pane.WorkspaceRootPane
import com.tyron.code.project.BasicWorkspaceManager
import com.tyron.code.project.ModuleManager
import com.tyron.code.project.Workspace
import com.tyron.code.project.WorkspaceManager
import com.tyron.code.project.file.FileManager
import com.tyron.code.project.file.FileManagerImpl
import com.tyron.code.project.impl.CodeAssistModuleManager
import org.koin.dsl.module
import java.util.concurrent.Executors

val mainModule = module {

    single<WorkspaceManager> { BasicWorkspaceManager() }
    single { WorkspaceRootPane(get()) }

    factory<Workspace> {
        get<WorkspaceManager>().current
    }

    scope<WorkspaceImpl> {
        scoped<FileManager> {
            FileManagerImpl(get<Workspace>().root.toUri(), listOf(), Executors.newSingleThreadExecutor())
        }

        scoped<ModuleManager> {
            CodeAssistModuleManager(get(), get<Workspace>().root)
        }
    }
}