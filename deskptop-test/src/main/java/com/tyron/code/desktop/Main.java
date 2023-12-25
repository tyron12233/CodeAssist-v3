package com.tyron.code.desktop;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> new TestEditor().setVisible(true));
    }
}
