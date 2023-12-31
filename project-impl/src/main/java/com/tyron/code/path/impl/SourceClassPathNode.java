package com.tyron.code.path.impl;

import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.path.PathNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SourceClassPathNode extends AbstractPathNode<String, SourceClassInfo> {

    public static final String TYPE_ID = "source-class-info";

    /**
     * Node with parent.
     *
     * @param parent
     * 		Parent node.
     * @param info
     * 		Class value.
     *
     * @see DirectoryPathNode#child(ClassInfo)
     */
    public SourceClassPathNode(@Nullable DirectoryPathNode parent, @NotNull SourceClassInfo info) {
        super(TYPE_ID, parent, SourceClassInfo.class, info);
    }

    @Override
    public DirectoryPathNode getParent() {
        return (DirectoryPathNode) super.getParent();
    }

    @Override
    public @NotNull Set<String> directParentTypeIds() {
        return Set.of(DirectoryPathNode.TYPE_ID);
    }

    @Override
    public int localCompare(PathNode<?> o) {
        if (o instanceof SourceClassPathNode classPathNode) {
            String name = getValue().getName();
            String otherName = classPathNode.getValue().getName();
            return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
        }
        return 0;
    }
}
