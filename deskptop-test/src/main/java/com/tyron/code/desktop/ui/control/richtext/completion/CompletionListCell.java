package com.tyron.code.desktop.ui.control.richtext.completion;

import com.tyron.code.desktop.ui.control.IconView;
import com.tyron.code.desktop.util.Icons;
import com.tyron.code.java.completion.CompletionCandidate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CompletionListCell extends ListCell<CompletionCandidate> {

    private final CompletionCell completionCell;

    public CompletionListCell() {
        completionCell = new CompletionCell();
    }

    @Override
    protected void updateItem(CompletionCandidate item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            completionCell.setCompletionCandidate(item);
            setGraphic(completionCell);
        }
    }

    private static class CompletionCell extends HBox {

        private final IconView iconView = Icons.getIconView(Icons.CLASS, 24);
        private final Label label = new Label("");
        private final Label labelDesc = new Label("");
        private final Label details = new Label("");

        public CompletionCell() {
            setFillHeight(true);
            getChildren().add(iconView);
            getChildren().add(label);
            getChildren().add(labelDesc);
            getChildren().add(details);


            setAlignment(Pos.CENTER_LEFT);
            label.setPadding(new Insets(0, 0, 0, 8));
            label.setAlignment(Pos.CENTER_LEFT);
            label.setFont(new Font("Jetbrains Mono", 14));

            labelDesc.setFont(new Font("Jetbrains Mono", 12));
            labelDesc.setPadding(new Insets(0, 0, 0, 4));
            label.setAlignment(Pos.CENTER_LEFT);
            labelDesc.setTextOverrun(OverrunStyle.ELLIPSIS);

            VBox box = new VBox();
            box.setAlignment(Pos.CENTER_RIGHT);
            getChildren().add(box);

            details.setFont(new Font("Jetbrains Mono", 14));
            details.setAlignment(Pos.CENTER_RIGHT);
            box.getChildren().add(details);
        }

        public void setCompletionCandidate(CompletionCandidate item) {
            iconView.setImage(Icons.getImage(getIcon(item.getKind())));
            label.setText(item.getName());
            labelDesc.setText(item.getNameDescription().orElse(""));
            details.setText(item.getDetail().orElse(""));
        }

        private String getIcon(CompletionCandidate.Kind kind) {
            return switch (kind) {
                case CLASS -> Icons.CLASS;
                case INTERFACE -> Icons.INTERFACE;
                case ENUM -> Icons.ENUM;
                case METHOD -> Icons.METHOD;
                case FIELD -> Icons.FIELD;
                default -> Icons.CLASS;
            };
        }

    }
}
