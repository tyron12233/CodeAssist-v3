package com.tyron.code.project;

import com.tyron.code.project.model.module.Module;

import javax.annotation.Nullable;

public interface ProjectManager {

    Module getCurrent();

    default boolean setCurrent(@Nullable Module projectModule) {
        Module current = getCurrent();
        if (current == null) {

        }

        throw new UnsupportedOperationException();
    }

    /**
     * Effectively {@link #setCurrent(Module)} except any blocking conditions are bypassed.
     * <br>
     * Listeners for open/close events must be called when implementing this.
     *
     * @param projectModule
     * 		New workspace to set as the active workspace.
     */
    void setCurrentIgnoringConditions(@Nullable Module projectModule);

    /**
     * Closes the current project.
     *
     * @return {@code true} on success.
     */
    default boolean closeCurrent() {
        if (getCurrent() != null)
            return setCurrent(null);
        return true;
    }
}
