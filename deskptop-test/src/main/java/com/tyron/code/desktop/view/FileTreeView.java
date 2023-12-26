package com.tyron.code.desktop.view;

import com.tyron.code.desktop.model.FileTreeModel;

import javax.swing.*;

// View
public class FileTreeView extends JScrollPane {

    private final JTree fileTree;
    private final FileTreeModel fileTreeModel;

    public FileTreeView(FileTreeModel model) {
        this.fileTreeModel = model;
        this.fileTree = new JTree(model.getTreeModel());
        setViewportView(fileTree);
    }

    public JTree getFileTree() {
        return fileTree;
    }
}