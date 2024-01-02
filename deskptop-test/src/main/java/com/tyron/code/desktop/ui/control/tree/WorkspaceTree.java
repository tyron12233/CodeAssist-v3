package com.tyron.code.desktop.ui.control.tree;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.fasterxml.jackson.databind.util.Named;
import com.tyron.code.desktop.util.FxThreadUtils;
import com.tyron.code.desktop.util.Icons;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;
import com.tyron.code.project.util.StringUtil;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.util.Unchecked;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public class WorkspaceTree extends TreeView<Path> {

    private static final Comparator<Named> PATH_COMPARATOR = (o1, o2) -> {
        String a = o1.getName();
        String b = o2.getName();
        return compareFilePaths(a, b);
    };
    private FileTreeItem root;
    private Workspace workspace;

    public WorkspaceTree() {
//        setShowRoot(false);
        setCellFactory(param -> new FileTreeCell(workspace));
        getStyleClass().addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE);
    }

    public void createWorkspaceRoot(@Nullable Workspace workspace) {
        if (workspace == null) {
            root = null;
        } else {
            RootModule module = workspace.getModule();
            root = Unchecked.get(() -> FileTreeItemBuilder.build(module.getRootDirectory()));
        }

        this.workspace = workspace;

        FxThreadUtils.run(() -> setRoot(root));
    }

    @SuppressWarnings("StringEquality")
    private static int compareFilePaths(@NotNull String a, @NotNull String b) {
        String directoryPathA = StringUtil.cutOffAtLast(a, '/');
        String directoryPathB = StringUtil.cutOffAtLast(b, '/');
        if (!Objects.equals(directoryPathA, directoryPathB)) {
            // The directory path is the input path (same reference) if there is no '/'.
            // We always want root paths to be shown first since we group them in a container directory anyways.
            if (directoryPathA == a && directoryPathB != b) {
                return -1;
            }
            if (directoryPathA != a && directoryPathB == b) {
                return 1;
            }

            // We want subdirectories to be shown first over files in the directory.
            if (directoryPathB.startsWith(directoryPathA)) {
                return 1;
            } else if (directoryPathA.startsWith(directoryPathB))
                return -1;
        }

        return a.compareTo(b);
    }
}
