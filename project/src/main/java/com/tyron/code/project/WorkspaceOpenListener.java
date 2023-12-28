package com.tyron.code.project;

import javax.annotation.Nonnull;

/**
 * Listener for when new workspaces are opened.
 */
public interface WorkspaceOpenListener {
    /**
     * Called when {@link WorkspaceManager#setCurrent(Workspace)} passes.
     *
     * @param workspace New workspace assigned.
     */
    void onWorkspaceOpened(@Nonnull Workspace workspace);
}