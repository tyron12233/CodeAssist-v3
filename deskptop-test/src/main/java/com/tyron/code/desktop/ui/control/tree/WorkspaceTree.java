package com.tyron.code.desktop.ui.control.tree;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.fasterxml.jackson.databind.util.Named;
import com.tyron.code.desktop.services.navigation.Actions;
import com.tyron.code.desktop.services.navigation.SourceFileNavigable;
import com.tyron.code.desktop.ui.control.FontIconView;
import com.tyron.code.desktop.util.FxThreadUtils;
import com.tyron.code.desktop.util.Icons;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.path.impl.SourceClassPathNode;
import com.tyron.code.path.impl.WorkspacePathNode;
import com.tyron.code.project.dependency.DependencyUtil;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;
import com.tyron.code.project.model.module.SourceModule;
import com.tyron.code.project.util.StringUtil;
import com.tyron.code.path.PathNode;
import com.tyron.code.path.impl.DirectoryPathNode;
import com.tyron.code.path.impl.ModulePathNode;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.util.Unchecked;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.koin.java.KoinJavaComponent;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WorkspaceTree extends TreeView<PathNode<?>> {

    private static final Comparator<Named> PATH_COMPARATOR = (o1, o2) -> {
        String a = o1.getName();
        String b = o2.getName();
        return compareFilePaths(a, b);
    };
    private WorkspaceTreeNode root;
    private WorkspacePathNode rootPath;
    private Workspace workspace;

    public WorkspaceTree() {
//        setShowRoot(false);
        setCellFactory(param -> new TreeCell<>() {
            @Override
            protected void updateItem(PathNode<?> item, boolean empty) {
                super.updateItem(item, empty);

                setText(textOf(item));
                setGraphic(graphicOf(item));
            }

            private Node graphicOf(PathNode<?> item) {
                if (item instanceof WorkspacePathNode) {
                    return new FontIconView(CarbonIcons.WORKSPACE);
                }
                if (item instanceof DirectoryPathNode directoryPathNode) {
                    return new FontIconView(CarbonIcons.FOLDER);
                }
                if (item instanceof SourceClassPathNode) {
                    return Icons.getIconView(Icons.CLASS);
                }
                if (item instanceof ModulePathNode modulePathNode) {
                    Module value = modulePathNode.getValue();
                    if (value instanceof JavaModule javaModule) {
                        return new FontIconView(CarbonIcons.FOLDER_SHARED);
                    }
                }
                return null;
            }

            private String textOf(PathNode<?> item) {
                if (item instanceof DirectoryPathNode directoryPathNode) {
                    return StringUtil.shortenPath(directoryPathNode.getValue());
                }
                if (item instanceof SourceClassPathNode s) {
                    this.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            Actions actions = KoinJavaComponent.get(Actions.class);
                            SourceFileNavigable n = actions.gotoDeclaration(s);
                            n.requestFocus();
                        }
                    });
                    return s.getValue().getSourceFileName();
                }
                if (item instanceof ModulePathNode modulePathNode) {
                    Module value = modulePathNode.getValue();
                    if (value instanceof JavaModule javaModule) {
                        return javaModule.getName() + " (Java)";
                    }
                }

                if (item instanceof WorkspacePathNode workspacePathNode) {
                    return workspacePathNode.getValue().getName() + " (Root)";
                }
                return "";
            }
        });
        getStyleClass().addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE);
    }

    public void createWorkspaceRoot(@Nullable Workspace workspace) {
        if (workspace == null) {
            root = null;
        } else {
            RootModule module = workspace.getModule();
            rootPath = new WorkspacePathNode(module);
            root = new WorkspaceTreeNode(rootPath);

            module.getIncludedModules().forEach(includedModule -> {
                ModulePathNode modulePathNode = new ModulePathNode(includedModule);

                if (includedModule instanceof JavaModule javaModule) {

                    Map<String, DirectoryPathNode> directories = new HashMap<>();

                    try (var stream = Unchecked.get(() -> Files.walk(javaModule.getRootDirectory()))) {
                        stream.filter(Files::isDirectory)
                                .filter(it -> !it.equals(javaModule.getRootDirectory()))
                                .forEach(it -> {
                                    Path relative = javaModule.getRootDirectory().relativize(it);
                                    DirectoryPathNode directoryPathNode = directories.computeIfAbsent(relative.toString(), d -> new DirectoryPathNode(modulePathNode, d));
                                    WorkspaceTreeNode.getOrInsertIntoTree(root, directoryPathNode);
                                });
                    }
                    Set<SourceClassInfo> files = javaModule.getFiles();
                    files.forEach(file -> {
                        Path path = javaModule.getRootDirectory().relativize(file.getPath());
                        String directoryPath = path.getParent().toString();
                        String fileName = path.getFileName().toString();

                        DirectoryPathNode directoryPathNode = directories.computeIfAbsent(directoryPath, d -> new DirectoryPathNode(modulePathNode, d));
                        SourceClassPathNode classPathNode = new SourceClassPathNode(directoryPathNode, file);
                        WorkspaceTreeNode.getOrInsertIntoTree(root, classPathNode, true);
                    });
                }
            });

        }

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
