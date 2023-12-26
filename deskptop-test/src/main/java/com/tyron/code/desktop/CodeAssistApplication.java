package com.tyron.code.desktop;

import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.FileManagerImpl;
import com.tyron.code.project.impl.FileSystemModuleManager;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public class CodeAssistApplication {

    private final FileManager fileManager;
    private final ModuleManager moduleManager;

    public CodeAssistApplication(Path projectRoot) {
        this.fileManager = new FileManagerImpl(projectRoot.toUri(), List.of(), Executors.newSingleThreadExecutor());
        this.moduleManager = new FileSystemModuleManager(fileManager, projectRoot);
    }

    public void initialize() {

    }

    public FileManager getFileManager() {
        return fileManager;
    }
}
