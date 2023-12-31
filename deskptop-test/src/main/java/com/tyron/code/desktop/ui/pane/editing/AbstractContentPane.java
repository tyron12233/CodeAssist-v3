package com.tyron.code.desktop.ui.pane.editing;

import com.tyron.code.desktop.services.navigation.Navigable;
import com.tyron.code.desktop.services.navigation.UpdatableNavigable;
import com.tyron.code.info.Info;
import com.tyron.code.path.PathNode;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base type for common content panes displaying {@link Info} values.
 *
 * @param <P>
 * 		Info path type.
 *
 */
public abstract class AbstractContentPane<P extends PathNode<?>> extends BorderPane implements UpdatableNavigable {

    protected final List<Consumer<P>> pathUpdateListeners = new ArrayList<>();
    protected final List<Navigable> children = new ArrayList<>();
//    protected SideTabs sideTabs;
    protected P path;

    /**
     * Clear the display.
     */
    protected void clearDisplay() {
        // Remove navigable child.
        if (getCenter() instanceof Navigable navigable) {
            children.remove(navigable);
        }

        // Remove display node.
        setCenter(null);
    }

    /**
     * @param node
     * 		Node to display.
     */
    protected void setDisplay(Node node) {
        // Remove old navigable child.
        Node old = getCenter();
        if (old instanceof Navigable navigableOld) {
            children.remove(navigableOld);
        }

        // Add navigable child.
        if (node instanceof Navigable navigableNode) {
            children.add(navigableNode);
        }

        // Set display node.
        setCenter(node);
    }

    /**
     * Refresh the display.
     */
    protected void refreshDisplay() {
        // Refresh display
        clearDisplay();
        generateDisplay();

        // Refresh UI with path
        if (getCenter() instanceof UpdatableNavigable updatable) {
            updatable.onUpdatePath(getPath());
        }
    }

    /**
     * Generate display for the content denoted by {@link #getPath() the path node}.
     * Children implementing this should call {@link #setDisplay(Node)}.
     */
    protected abstract void generateDisplay();

    /**
     * @param listener
     * 		Listener to add.
     */
    public void addPathUpdateListener(Consumer<P> listener) {
        pathUpdateListeners.add(listener);
    }

    @NotNull
    @Override
    public Collection<Navigable> getNavigableChildren() {
        return children;
    }

    @Override
    public void disable() {
        children.forEach(Navigable::disable);
        pathUpdateListeners.clear();
        setDisable(true);
    }
}
