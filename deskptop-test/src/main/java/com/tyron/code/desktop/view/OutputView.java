package com.tyron.code.desktop.view;

import com.tyron.code.desktop.model.OutputModel;

import javax.swing.*;

public class OutputView extends JScrollPane {
    private JTextArea outputArea;

    public OutputView(OutputModel model) {
        outputArea = new JTextArea();
        setViewportView(outputArea);
    }

    public void updateView(OutputModel model) {
        outputArea.setText(model.getOutputText());
    }
}