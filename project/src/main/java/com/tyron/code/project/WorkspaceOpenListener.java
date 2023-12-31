package com.tyron.code.project;

import org.jetbrains.annotations.NotNull;

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
    void onWorkspaceOpened(@NotNull Workspace workspace);
}