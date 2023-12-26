package com.tyron.code.desktop.model;

public class OutputModel {
    private StringBuilder outputText;

    public OutputModel() {
        outputText = new StringBuilder();
    }

    public String getOutputText() {
        return outputText.toString();
    }

    public void appendToOutput(String text) {
        outputText.append(text);
    }
}
