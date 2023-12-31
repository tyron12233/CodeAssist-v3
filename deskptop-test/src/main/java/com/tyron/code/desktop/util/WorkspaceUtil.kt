package com.tyron.code.desktop.util

import com.tyron.code.project.Workspace
import org.koin.core.component.getScopeId
import org.koin.core.scope.get
import org.koin.mp.KoinPlatform.getKoin

object WorkspaceUtil {
    /**
     * Get the workspace scoped instance of the given class.
     */
    @JvmStatic
    fun <T> Workspace.getScoped(clazz : Class<T>) : T {
        return getKoin().getScope(this.getScopeId()).get(clazz)
    }
}