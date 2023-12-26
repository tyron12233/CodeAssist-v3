package com.tyron.code.desktop;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.setProperty("flatlaf.menuBarEmbedded", "true");
        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
        FlatMacDarkLaf.setup();
//
//        Path root = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/deskptop-test/src/main/resources/TestProject");
//        FileManager fileManager = new FileManagerImpl(root.toUri(), List.of(), Executors.newSingleThreadExecutor());
//        ModuleManager moduleManager = new CodeAssistModuleManager(fileManager, root);
//        moduleManager.initialize();
//
//        SwingUtilities.invokeLater(() -> new BasicIDE((ProjectModule) moduleManager.getRootModule()).setVisible(true));
        SwingUtilities.invokeLater(() -> new TestEditor().setVisible(true));
    }
}
