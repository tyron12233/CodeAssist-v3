package com.tyron.code.path.impl;

import com.tyron.code.info.FileInfo;
import com.tyron.code.path.PathNode;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FilePathNode extends AbstractPathNode<String, FileInfo> {

    /**
     * Type identifier for file nodes.
     */
    public static final String TYPE_ID = "file";


    /**
     * Node without parent.
     *
     * @param info
     * 		File value.
     */
    public FilePathNode(@NotNull FileInfo info) {
        this(null, info);
    }

    /**
     * Node with parent.
     *
     * @param parent
     * 		Parent node.
     * @param info
     * 		File value.
     *
     * @see DirectoryPathNode#child(FileInfo)
     */
    public FilePathNode(@Nullable DirectoryPathNode parent, @NotNull FileInfo info) {
        super(TYPE_ID, parent, FileInfo.class, info);
    }

    @Override
    public DirectoryPathNode getParent() {
        return (DirectoryPathNode) super.getParent();
    }

    @NotNull
    @Override
    public Set<String> directParentTypeIds() {
        return Set.of(DirectoryPathNode.TYPE_ID, ModulePathNode.TYPE_ID);
    }

    @Override
    public int localCompare(PathNode<?> o) {
        if (o instanceof FilePathNode fileNode) {
            String name = getValue().getName();
            String otherName = fileNode.getValue().getName();
            return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
        }
        return 0;
    }
}
