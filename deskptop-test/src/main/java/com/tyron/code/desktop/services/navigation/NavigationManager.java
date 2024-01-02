package com.tyron.code.desktop.services.navigation;

import com.tyron.code.desktop.ui.docking.DockingManager;
import com.tyron.code.desktop.ui.docking.DockingTab;
import com.tyron.code.logging.Logging;
import com.tyron.code.path.PathNode;
import com.tyron.code.path.impl.AbstractPathNode;
import com.tyron.code.project.WorkspaceManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class NavigationManager implements Navigable {
    public static final String ID = "navigation";
    private static final Logger logger = Logging.get(NavigationManager.class);
    private final List<Navigable> children = new ArrayList<>();
    private final Map<Navigable, DockingTab> childrenToTab = new IdentityHashMap<>();
    private final Map<DockingTab, NavigableSpy> tabToSpy = new IdentityHashMap<>();
    private PathNode<?> path = new DummyInitialNode();

    public NavigationManager(@NotNull DockingManager dockingManager, @NotNull WorkspaceManager workspaceManager) {
        dockingManager.addTabCreationListener((parent, tab) -> {
            ObjectProperty<Node> contentProperty = tab.contentProperty();

            // Create spy for the tab.
            NavigableSpy spy = new NavigableSpy(tab);
            tabToSpy.put(tab, spy);

            // Add listener, so if content changes we are made aware of the changes.
            contentProperty.addListener(spy);

            // Record initial value.
            spy.changed(contentProperty, null, contentProperty.getValue());
        });

        dockingManager.addTabClosureListener(((parent, tab) -> {
            // The tab is closed, remove its spy lookup.
            NavigableSpy spy = tabToSpy.remove(tab);
            if (spy == null) {
                logger.warn("Tab {} was closed, but had no associated content spy instance", tab.getText());
                return;
            }

            // Remove content from navigation tracking.
            spy.remove(tab.getContent());

            // Remove the listener from the tab.
            tab.contentProperty().removeListener(spy);
        }));
    }

    @NotNull
    @Override
    public PathNode<?> getPath() {
        return path;
    }

    @NotNull
    @Override
    public Collection<Navigable> getNavigableChildren() {
        return children;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void disable() {

    }

    /**
     * Listener to update {@link #children}.
     */
    private class NavigableSpy implements ChangeListener<Node> {
        private final DockingTab tab;

        public NavigableSpy(DockingTab tab) {
            this.tab = tab;
        }

        @Override
        public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
            remove(oldValue);
            add(newValue);
        }

        void add(Node value) {
            if (value instanceof Navigable navigable) {
                children.add(navigable);
                childrenToTab.put(navigable, tab);
            }
        }

        void remove(Node value) {
            if (value instanceof Navigable navigable) {
                children.remove(navigable);
                childrenToTab.remove(navigable);

//                // For dependent beans, we are likely not going to see them ever again.
//                // Call disable to clean them up.
//                if (value.getClass().getDeclaredAnnotation(Dependent.class) != null)
                    navigable.disable();
            }
        }
    }


    /**
     * Dummy node for initial state of {@link #path}.
     */
    private static class DummyInitialNode extends AbstractPathNode<Object, Object> {
        private DummyInitialNode() {
            super("dummy", null, Object.class, new Object());
        }

        @NotNull
        @Override
        public Set<String> directParentTypeIds() {
            return Collections.emptySet();
        }

        @Override
        public int localCompare(PathNode<?> o) {
            return -1;
        }
    }
}
