package com.tyron.code.desktop.ui.pane.editing;

import com.tyron.code.desktop.services.navigation.SourceFileNavigable;
import com.tyron.code.desktop.services.navigation.UpdatableNavigable;
import com.tyron.code.path.PathNode;
import com.tyron.code.path.impl.SourceClassPathNode;
import org.jetbrains.annotations.NotNull;

public class SourcePane extends AbstractContentPane<SourceClassPathNode> implements SourceFileNavigable {
    @NotNull
    @Override
    public SourceClassPathNode getPath() {
        return path;
    }

    @Override
    public void onUpdatePath(@NotNull PathNode<?> path) {
        if (path instanceof SourceClassPathNode sourceClassPathNode) {
            this.path = sourceClassPathNode;
            pathUpdateListeners.forEach(listener -> listener.accept(sourceClassPathNode));

            if (getCenter() == null) {
                generateDisplay();
            }

            // Notify children of change.
            getNavigableChildren().forEach(child -> {
                if (child instanceof UpdatableNavigable updatable) {
                    updatable.onUpdatePath(path);
                }
            });
        }
    }

    @Override
    protected void generateDisplay() {
        setDisplay(new JavaEditorPane());
    }
}
