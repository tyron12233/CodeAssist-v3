package com.tyron.code.project;

import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Condition applied to {@link WorkspaceManager} to prevent closure of an active workspace for when
 * {@link WorkspaceManager#setCurrent(Workspace)} is called.
 */
public interface WorkspaceCloseCondition {
    /**
     * @param current Current workspace.
     * @return {@code true} when the operation is allowed.
     */
    boolean canClose(@NotNull Workspace current);
}