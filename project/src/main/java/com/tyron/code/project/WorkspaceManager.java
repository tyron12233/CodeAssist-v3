package com.tyron.code.project;

import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 *
 */
public interface WorkspaceManager {


    Workspace getCurrent();

    default boolean setCurrent(@Nullable Workspace workspace) {
         Workspace current = getCurrent();
        if (current == null) {
            // If there is no current workspace, then just assign it.
            setCurrentIgnoringConditions(workspace);
            return true;
        } else if (getWorkspaceCloseConditions().stream()
                .allMatch(condition -> condition.canClose(current))) {
            // Otherwise, check if the conditions allow for closing the prior workspace.
            // If so, then assign the new workspace.
            setCurrentIgnoringConditions(workspace);
            return true;
        }
        // Workspace closure conditions not met, assignment denied.
        return false;
    }

    /**
     * Effectively {@link #setCurrent(Workspace)} except any blocking conditions are bypassed.
     * <br>
     * Listeners for open/close events must be called when implementing this.
     *
     * @param workspace
     * 		New workspace to set as the active workspace.
     */
    void setCurrentIgnoringConditions(@Nullable Workspace workspace);

    /**
     * Closes the current project.
     *
     * @return {@code true} on success.
     */
    default boolean closeCurrent() {
        if (getCurrent() != null) {
            return setCurrent(null);
        }
        return true;
    }

    /**
     * @return Conditions in the manager that can prevent {@link #setCurrent(Workspace)} from going through.
     */
    @NotNull
    List<WorkspaceCloseCondition> getWorkspaceCloseConditions();

    /**
     * @param condition
     * 		Condition to add.
     */
    void addWorkspaceCloseCondition(WorkspaceCloseCondition condition);

    /**
     * @param condition
     * 		Condition to remove.
     */
    void removeWorkspaceCloseCondition(WorkspaceCloseCondition condition);

    /**
     * @return Listeners for when a new workspace is assigned as the current one.
     */
    @NotNull
    List<WorkspaceOpenListener> getWorkspaceOpenListeners();

    /**
     * @param listener
     * 		Listener to add.
     */
    void addWorkspaceOpenListener(WorkspaceOpenListener listener);

    /**
     * @param listener
     * 		Listener to remove.
     */
    void removeWorkspaceOpenListener(WorkspaceOpenListener listener);

    /**
     * @return Listeners for when the current workspace is removed as being current.
     */
    @NotNull
    List<WorkspaceCloseListener> getWorkspaceCloseListeners();

    /**
     * @param listener
     * 		Listener to add.
     */
    void addWorkspaceCloseListener(WorkspaceCloseListener listener);

    /**
     * @param listener
     * 		Listener to remove.
     */
    void removeWorkspaceCloseListener(WorkspaceCloseListener listener);

}
