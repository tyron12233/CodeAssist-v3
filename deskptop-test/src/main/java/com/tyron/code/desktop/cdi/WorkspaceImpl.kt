package com.tyron.code.desktop.cdi

import com.tyron.code.project.Workspace
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.scope.Scope
import java.nio.file.Path

class WorkspaceImpl(private val root: Path) : Workspace, KoinScopeComponent {


    override val scope: Scope by lazy { createScope(this as Workspace) }

    override fun close() {
        scope.close()
    }

    override fun getRoot(): Path {
        return this.root
    }
}