package com.tyron.code.desktop.ui.pane;

import atlantafx.base.theme.Styles;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.graph.GraphPrinter;
import com.tyron.code.project.model.module.RootModule;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class WorkspaceInformationPane extends BorderPane {

    public WorkspaceInformationPane(Workspace workspace) {
        Grid content = new Grid();
        content.setPadding(new Insets(5));
        content.prefWidthProperty().bind(widthProperty());
        ScrollPane scroll = new ScrollPane(content);
        setCenter(scroll);
        getStyleClass().add("background");

        RootModule module = workspace.getModule();
        Label title = new Label(module.getName());
        Label subtitle = new Label(String.format("%d included modules", module.getIncludedModules().size()));
        title.getStyleClass().add(Styles.TITLE_4);
        subtitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        VBox wrapper = new VBox(title, subtitle);
        content.add(wrapper, 0, content.getRowCount(), 2, 2);
    }

    private static class Grid extends GridPane {
        private Grid() {
            setVgap(5);
            setHgap(5);
            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();
            column1.setPercentWidth(25);
            column2.setPercentWidth(75);
            getColumnConstraints().addAll(column1, column2);
        }
    }
}
