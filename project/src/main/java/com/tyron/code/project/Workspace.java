package com.tyron.code.project;

import com.tyron.code.project.model.module.RootModule;

import java.nio.file.Path;

public interface Workspace {

    enum State {
        UNINITIALIZED,

        INITIALIZING,

        INITIALIZED
    }

    void close();

    Path getRoot();

    State getState();

    void addWorkspaceStateChangeListener(WorkspaceStateListener listener);

    void removeWorkspaceStateChangeListener(WorkspaceStateListener listener);

    RootModule getModule();
}
