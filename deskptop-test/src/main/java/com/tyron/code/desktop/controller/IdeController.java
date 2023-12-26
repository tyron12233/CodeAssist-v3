package com.tyron.code.desktop.controller;

import com.tyron.code.desktop.completion.BasicIDE;
import com.tyron.code.desktop.model.FileTreeModel;
import com.tyron.code.project.model.module.Module;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class IdeController {
    private final BasicIDE view;
    private final FileTreeModel fileTreeModel;
    private final Module rootProject;

    public IdeController(BasicIDE view, Module root, FileTreeModel model) {
        this.view = view;
        this.fileTreeModel = model;

        this.rootProject = root;
    }

    public void initialize() {
        fileTreeModel.setRootDirectory(rootProject.getRootDirectory().toFile());
        MouseListener fileTreeClickListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    TreeSelectionModel selectionModel = view.getFileTreeView().getFileTree().getSelectionModel();
                    TreePath selectionPath = selectionModel.getSelectionPath();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                    if (node.isLeaf()) {

                    }
                }
            }
        };
        view.getFileTreeView().getFileTree().addMouseListener(fileTreeClickListener);
    }
}
