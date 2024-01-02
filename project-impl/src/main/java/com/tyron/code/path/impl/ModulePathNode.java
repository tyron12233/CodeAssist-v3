package com.tyron.code.path.impl;

import com.tyron.code.path.PathNode;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class ModulePathNode extends AbstractPathNode<Object, Module>{

    public static final String TYPE_ID = "module";

    public ModulePathNode(Module module) {
        super(TYPE_ID, null, Module.class, module);
    }

    @Override
    public @NotNull Set<String> directParentTypeIds() {
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
