package com.tyron.code.desktop.ui.pane;

import com.tyron.code.desktop.ui.control.tree.WorkspaceTree;
import com.tyron.code.project.Workspace;
import javafx.scene.layout.BorderPane;

public class WorkspaceExplorerPane extends BorderPane {

    private final WorkspaceTree workspaceTree;

    public WorkspaceExplorerPane(Workspace workspace) {
        workspaceTree = new WorkspaceTree();

        setCenter(workspaceTree);

        workspaceTree.createWorkspaceRoot(workspace);
    }
}
