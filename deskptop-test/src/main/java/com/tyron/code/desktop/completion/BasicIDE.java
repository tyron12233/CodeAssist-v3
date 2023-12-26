package com.tyron.code.desktop.completion;

import com.tyron.code.desktop.controller.IdeController;
import com.tyron.code.desktop.model.EditorModel;
import com.tyron.code.desktop.model.FileTreeModel;
import com.tyron.code.desktop.model.OutputModel;
import com.tyron.code.desktop.view.EditorView;
import com.tyron.code.desktop.view.FileTreeView;
import com.tyron.code.desktop.view.OutputView;
import com.tyron.code.project.model.module.Module;

import javax.swing.*;
import java.awt.*;

public class BasicIDE extends JFrame {

    private final FileTreeModel fileTreeModel;
    private final EditorModel editorModel;
    private final OutputModel outputModel;

    private final FileTreeView fileTreeView;
    private final EditorView editorView;
    private final OutputView outputView;

    public BasicIDE(Module rootProject) {
        fileTreeModel = new FileTreeModel();
        editorModel = new EditorModel();
        outputModel = new OutputModel();

        fileTreeView = new FileTreeView(fileTreeModel);
        editorView = new EditorView(editorModel);
        outputView = new OutputView(outputModel);

        // Set up the main frame
        setTitle("Resizable IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setResizable(true);


        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTreeView, editorView);
        leftSplitPane.setDividerLocation(0.2);
        leftSplitPane.setResizeWeight(0.15);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftSplitPane, outputView);
        mainSplitPane.setDividerLocation(0.6);
        mainSplitPane.setResizeWeight(0.6);

        add(mainSplitPane, BorderLayout.CENTER);

        // Create and add the toolbar
        JMenuBar toolbar = createToolbar();
        setJMenuBar(toolbar);

        IdeController ideController = new IdeController(this, rootProject, fileTreeModel);
        ideController.initialize();
    }

    public FileTreeView getFileTreeView() {
        return fileTreeView;
    }

    public OutputView getOutputView() {
        return outputView;
    }

    public EditorView getEditorView() {
        return editorView;
    }

    private JMenuBar createToolbar() {
        JMenuBar toolbar = new JMenuBar();

        var runButton = new JMenuItem("Run");
        var saveButton = new JMenuItem("Save");


        toolbar.add(runButton);
        toolbar.add(saveButton);

        return toolbar;
    }
}
