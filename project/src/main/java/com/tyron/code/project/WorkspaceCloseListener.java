package com.tyron.code.project;

import com.tyron.code.project.model.module.Module;

import javax.annotation.Nonnull;

/**
 * Listener for when old workspaces are closed.
 */
public interface WorkspaceCloseListener {
    /**
     * Called when {@link WorkspaceManager#setCurrent(Workspace)} passes and a prior workspace is removed.
     *
     * @param workspace New workspace module assigned.
     */
    void onWorkspaceClosed(@Nonnull Workspace workspace);
}