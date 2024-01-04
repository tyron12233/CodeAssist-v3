package com.tyron.code.desktop.ui.control.tree;

import com.tyron.code.desktop.services.navigation.Actions;
import com.tyron.code.desktop.util.Icons;
import com.tyron.code.desktop.util.WorkspaceUtil;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.model.module.ErroneousModule;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import javafx.scene.control.TreeCell;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileTreeCell extends TreeCell<Path> {

    private final Workspace workspace;

    public FileTreeCell(Workspace workspace) {

        this.workspace = workspace;
    }

    @Override
    protected void updateItem(Path item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (Files.isDirectory(item)) {
                configureDirectory(item);
            } else {
                configureFile(item);
            }

            setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Actions actions = WorkspaceUtil.getScoped(workspace, Actions.class);
                    actions.gotoDeclaration(item);
                }
            });
        }
    }

    private void configureFile(Path item) {
        ModuleManager moduleManager = WorkspaceUtil.getScoped(workspace, ModuleManager.class);
        Optional<Module> module = moduleManager.findModuleByFile(item);
        if (module.isPresent()) {
            Module includedModule = module.get();
            if (includedModule instanceof JavaModule javaModule) {
                if (javaModule.getModuleConfig().equals(item)) {
                    setText(item.getFileName().toString());
                    setGraphic(Icons.getImageView(Icons.FILE_CONFIG));
                    return;
                }

                if (item.getFileName().toString().endsWith(".java")) {
                    setText(item.getFileName().toString());
                    setGraphic(Icons.getImageView(Icons.CLASS));
                    return;
                }
            }
        }

        setText(item.getFileName().toString());
        setGraphic(Icons.getImageView(Icons.FILE_CLASS));
    }

    private void configureDirectory(Path item) {
        if (workspace.getRoot().equals(item)) {
            setText(item.getFileName() + " (root)");
            setGraphic(Icons.getImageView(Icons.FOLDER_MODULE));
            return;
        }

        ModuleManager moduleManager = WorkspaceUtil.getScoped(workspace, ModuleManager.class);
        Optional<Module> module = moduleManager.findModuleByFile(item);
        if (module.isPresent()) {
            Module includedModule = module.get();
            if (includedModule instanceof ErroneousModule) {
                setText(item.getFileName() + " (contains errors)");
                setGraphic(Icons.getImageView(Icons.FOLDER_MODULE));
                return;
            }
            if (includedModule.getRootDirectory().equals(item)) {
                setText(item.getFileName() + " (module)");
                setGraphic(Icons.getImageView(Icons.FOLDER_MODULE));
                return;
            }

            if (includedModule instanceof JavaModule javaModule) {
                if (javaModule.getSourceDirectory().equals(item)) {
                    setText(item.getFileName().toString());
                    setGraphic(Icons.getImageView(Icons.FOLDER_SRC));
                    return;
                }

                if (item.startsWith(javaModule.getSourceDirectory())) {
                    setText(item.getFileName().toString());
                    setGraphic(Icons.getImageView(Icons.FOLDER_PACKAGE));
                    return;
                }
            }
        }

        setText(item.getFileName().toString());
        setGraphic(Icons.getImageView(Icons.FOLDER));

    }

}
