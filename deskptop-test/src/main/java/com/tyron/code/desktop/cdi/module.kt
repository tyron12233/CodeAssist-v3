package com.tyron.code.desktop.cdi

import com.tyron.code.desktop.services.navigation.Actions
import com.tyron.code.desktop.services.navigation.NavigationManager
import com.tyron.code.desktop.ui.docking.DockingManager
import com.tyron.code.desktop.ui.pane.WelcomePane
import com.tyron.code.desktop.ui.pane.WorkspaceInformationPane
import com.tyron.code.desktop.ui.pane.WorkspaceRootPane
import com.tyron.code.java.analysis.Analyzer
import com.tyron.code.java.completion.Completor
import com.tyron.code.project.BasicWorkspaceManager
import com.tyron.code.project.ModuleManager
import com.tyron.code.project.Workspace
import com.tyron.code.project.WorkspaceManager
import com.tyron.code.project.file.FileManager
import com.tyron.code.project.file.FileManagerImpl
import com.tyron.code.project.impl.CodeAssistModuleManager
import com.tyron.code.project.impl.WorkspaceImpl
import com.tyron.code.project.model.module.JavaModule
import com.tyron.code.project.model.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.Executors

val mainModule = module {

    single<WorkspaceManager> { BasicWorkspaceManager() }
    single { DockingManager() }
    single { NavigationManager(get(), get()) }
    single { Actions(get(), get(), get()) }

    single { WorkspaceRootPane(get(), get()) }
    single { WelcomePane() }
    single { WorkspaceInformationPane(get()) }


    factory<Workspace> {
        get<WorkspaceManager>().current
    }

    scope<Workspace> {
        scoped<FileManager> {
            FileManagerImpl(get<Workspace>().root.toUri(), listOf(), Executors.newSingleThreadExecutor())
        }

        scoped<ModuleManager> {
            CodeAssistModuleManager(get(), get<Workspace>().root)
        }
    }

    scope<JavaModule> {
        scoped<Completor> {
            Completor(get(), get())
        }

        scoped<Analyzer> {
            Analyzer(get(), get<JavaModule>())
        }
    }
}