package com.tyron.code.project.impl;

import com.tyron.code.project.Workspace;
import com.tyron.code.project.WorkspaceStateListener;
import com.tyron.code.project.model.module.RootModule;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceImpl implements Workspace {

    private State currentState;

    private final Path root;

    private final List<WorkspaceStateListener> stateListeners;
    private RootModule rootModule;

    public WorkspaceImpl(Path root) {
        this.currentState = State.UNINITIALIZED;
        this.root = root;
        this.stateListeners = new ArrayList<>();
    }

    @Override
    public void close() {

    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public State getState() {
        return currentState;
    }

    @Override
    public void addWorkspaceStateChangeListener(WorkspaceStateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public void removeWorkspaceStateChangeListener(WorkspaceStateListener listener) {
        stateListeners.remove(listener);
    }

    public void setState(State state) {
        currentState = state;
        for (WorkspaceStateListener listener : List.copyOf(stateListeners)) {
            listener.onWorkspaceStateChanged(state);
        }
    }

    @Override
    public RootModule getModule() {
        return rootModule;
    }

    public void setRoot(RootModule module) {
        this.rootModule = module;
    }
}
