package com.tyron.code.path.impl;

import com.tyron.code.path.PathNode;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.model.module.RootModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class WorkspacePathNode extends AbstractPathNode<Object, RootModule> {

    /**
     * Type identifier for workspace nodes.
     */
    public static final String TYPE_ID = "workspace";


    /**
     * Node without parent.
     *
     * @param value Workspace value.
     */
    public WorkspacePathNode(@NotNull RootModule value) {
        super(TYPE_ID, null, RootModule.class, value);
    }


    @NotNull
    @Override
    public Set<String> directParentTypeIds() {
        return Collections.emptySet();
    }

    @Override
    public boolean isDescendantOf(@NotNull PathNode<?> other) {
        // Workspace is the root of all paths.
        // Only other workspace paths with the same value should count here.
        if (typeId().equals(other.typeId())) {
            return getValue().equals(other.getValue());
        }

        // We have no parents.
        return false;
    }

    @Override
    public int localCompare(PathNode<?> o) {
        return 0;
    }
}
