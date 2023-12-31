package com.tyron.code.project;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BasicWorkspaceManager implements WorkspaceManager {

    private static final Logger logger = Logger.getLogger("main");

    private final List<WorkspaceCloseCondition> closeConditions = new ArrayList<>();
    private final List<WorkspaceOpenListener> openListeners = new ArrayList<>();
    private final List<WorkspaceCloseListener> closeListeners = new ArrayList<>();

    private Workspace current;

    public BasicWorkspaceManager() {

    }

    @Override
    public Workspace getCurrent() {
        return current;
    }

    @Override
    public void setCurrentIgnoringConditions(@Nullable Workspace workspace) {
        if (current != null) {
            current.close();
            for (WorkspaceCloseListener listener : new ArrayList<>(closeListeners)) {
                try {
                    listener.onWorkspaceClosed(current);
                } catch (Throwable t) {
                    logger.throwing("Exception thrown by '{}' when closing workspace",
                            listener.getClass().getName(), t);
                }
            }
        }
        current = workspace;
        if (workspace != null) {
//            defaultModificationListeners.forEach(workspace::addWorkspaceModificationListener);
            for (WorkspaceOpenListener listener : new ArrayList<>(openListeners)) {
                try {
                    listener.onWorkspaceOpened(workspace);
                } catch (Throwable t) {
                    logger.throwing("Exception thrown by '{}' when opening workspace",
                            listener.getClass().getName(), t);
                }
            }
        }
    }

    @Nonnull
    @NotNull
    @Override
    public List<WorkspaceCloseCondition> getWorkspaceCloseConditions() {
        return closeConditions;
    }

    @Override
    public void addWorkspaceCloseCondition(WorkspaceCloseCondition condition) {
        closeConditions.add(condition);
    }

    @Override
    public void removeWorkspaceCloseCondition(WorkspaceCloseCondition condition) {
        closeConditions.remove(condition);
    }

    @Nonnull
    @NotNull
    @Override
    public List<WorkspaceOpenListener> getWorkspaceOpenListeners() {
        return openListeners;
    }

    @Override
    public void addWorkspaceOpenListener(WorkspaceOpenListener listener) {
        openListeners.add(listener);
    }

    @Override
    public void removeWorkspaceOpenListener(WorkspaceOpenListener listener) {
        openListeners.remove(listener);
    }

    @Nonnull
    @NotNull
    @Override
    public List<WorkspaceCloseListener> getWorkspaceCloseListeners() {
        return closeListeners;
    }

    @Override
    public void addWorkspaceCloseListener(WorkspaceCloseListener listener) {
        closeListeners.add(listener);
    }

    @Override
    public void removeWorkspaceCloseListener(WorkspaceCloseListener listener) {
        closeListeners.remove(listener);
    }
}
