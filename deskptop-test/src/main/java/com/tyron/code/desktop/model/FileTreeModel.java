package com.tyron.code.desktop.model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class FileTreeModel {

    private DefaultTreeModel treeModel;

    public FileTreeModel() {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Project"));
    }

    public void setRootDirectory(File rootDirectory) {
        DefaultMutableTreeNode root = buildFileTree(rootDirectory);
        treeModel.setRoot(root);
    }

    private DefaultMutableTreeNode buildFileTree(File rootDirectory) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootDirectory.getName());
        for (File file : rootDirectory.listFiles()) {
            buildTree(root, file);
        }
        return root;
    }

    private void buildTree(DefaultMutableTreeNode parent, File node) {
        if (node.isDirectory()) {
            DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(node.getName());
            parent.add(dirNode);
            for (File child : node.listFiles()) {
                buildTree(dirNode, child);
            }
        } else {
            parent.add(new DefaultMutableTreeNode(node.getName()));
        }
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }
}
