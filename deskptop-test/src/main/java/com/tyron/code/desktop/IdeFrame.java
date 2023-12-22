package com.tyron.code.desktop;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class IdeFrame extends JFrame {

    Path root = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/deskptop-test/src/test");
    private CodeAssistApplication application;

    private JTree jTree;
    private JPanel right;

    public IdeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        JPanel left = new JPanel();
        right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(900, 720));

        jTree = createFileTree();
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = jTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
//                    if(e.getClickCount() == 1) {
//                        mySingleClick(selRow, selPath);
//                    }
//                    else
                    if (e.getClickCount() == 2) {
                        assert selPath != null;
                        handleDoubleClick(selRow, selPath);
                    }
                }
            }
        };
        jTree.addMouseListener(ml);
        left.add(jTree);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(200);
        contentPane.add(splitPane);

        add(contentPane);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        initialize();
    }

    private void handleDoubleClick(int selRow, TreePath selPath) {
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selPath.getLastPathComponent();
        Path file = (Path) lastPathComponent.getUserObject();

        right.removeAll();
        right.add(new IdeEditor(application.getFileManager(), file));
    }

    private void initialize() {
        try {
            initializeInternal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeInternal() throws Exception {
        application = new CodeAssistApplication(root);
        application.initialize();

        jTree.setModel(createTreeModel(root));
    }

    private JTree createFileTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project");
        DefaultMutableTreeNode src = new DefaultMutableTreeNode("src");
        root.add(src);
        DefaultTreeModel model = new DefaultTreeModel(root);
        return new JTree(model);
    }

    public static DefaultTreeModel createTreeModel(Path path) throws IOException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(path.getFileName().toString());
        createNodes(root, path);
        return new DefaultTreeModel(root);
    }

    private static void createNodes(DefaultMutableTreeNode node, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> files = Files.list(path)) {
                for (Path child : files.toArray(Path[]::new)) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getFileName().toString());
                    childNode.setUserObject(child);
                    node.add(childNode);
                    createNodes(childNode, child);
                }
            }
        }
    }
}
