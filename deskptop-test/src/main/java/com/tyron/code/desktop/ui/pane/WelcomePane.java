package com.tyron.code.desktop.ui.pane;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class WelcomePane extends BorderPane {

    public WelcomePane() {
        setCenter(new Label("Welcome"));
    }
}
