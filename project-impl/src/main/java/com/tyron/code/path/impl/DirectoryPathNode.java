package com.tyron.code.path.impl;

import com.tyron.code.path.PathNode;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DirectoryPathNode extends AbstractPathNode<Module, String> {

    /**
     * Type identifier for directory nodes.
     */
    public static final String TYPE_ID = "directory";

    /**
     * Node without parent.
     *
     * @param directory
     * 		Directory name.
     */
    public DirectoryPathNode(@NotNull String directory) {
        this(null, directory);
    }

    /**
     * Node with parent.
     *
     * @param parent
     * 		Parent node.
     * @param directory
     * 		Directory name.
     */
    public DirectoryPathNode(@Nullable PathNode<Module> parent, @NotNull String directory) {
        super(TYPE_ID, parent, String.class, directory);
    }

    /**
     * @param directory
     * 		New directory name.
     *
     * @return New node with same parent, but different directory name value.
     */
    @NotNull
    public DirectoryPathNode withDirectory(@NotNull String directory) {
        return new DirectoryPathNode(getParent(), directory);
    }

    @Override
    public @NotNull Set<String> directParentTypeIds() {
        return Set.of(DirectoryPathNode.TYPE_ID);
    }

    @Override
    public boolean hasEqualOrChildValue(@NotNull PathNode<?> other) {
        if (other instanceof DirectoryPathNode otherDirectory) {
            String dir = getValue();
            String maybeParentDir = otherDirectory.getValue();

            // We cannot do just a basic 'startsWith' check on the path values since they do not
            // end with a trailing slash. This could lead to cases where:
            //  'co' is a parent value of 'com/foo'
            //
            // By doing an equals check, we allow for 'co' vs 'com' to fail but 'co' vs 'co' to pass,
            // and the following startsWith check with a slash allows us to not fall to the suffix issue described above.
            return dir.equals(maybeParentDir) || dir.startsWith(maybeParentDir + "/");
        }

        return super.hasEqualOrChildValue(other);
    }

    @Override
    public boolean isDescendantOf(@NotNull PathNode<?> other) {
        // Descendant check comparing between directories will check for containment within the local value's path.
        // This way 'a/b/c' is seen as a descendant of 'a/b'.
        if (typeId().equals(other.typeId())) {
            return hasEqualOrChildValue(other) && allParentsMatch(other);
        }

        return super.isDescendantOf(other);
    }

    @Override
    public int localCompare(PathNode<?> o) {
        if (o instanceof DirectoryPathNode pathNode) {
            String name = getValue();
            String otherName = pathNode.getValue();
            return String.CASE_INSENSITIVE_ORDER.compare(name, otherName);
        }
        return 0;
    }
}
