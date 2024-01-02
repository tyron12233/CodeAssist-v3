package com.tyron.code.project.impl;

import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSystemModuleManagerTest {

    private FileSystemModuleManager manager;
    private JavaModuleImpl module;

    private Path root;

    @BeforeEach
    public void setup() throws IOException {
        root = Files.createTempDirectory("project");
        SimpleFileManager fileManager = new SimpleFileManager();
        manager = new FileSystemModuleManager(fileManager, root);
        module = (JavaModuleImpl) manager.getRootModule();
    }

    @Test
    public void testAddOrUpdateFile() throws IOException {
        Path testJavaFile = root.resolve("Test.java");
        Files.createFile(testJavaFile);

        Set<SourceClassInfo> files = module.getSourceFiles();
        assertEquals(0, files.size());

        manager.addOrUpdateFile(testJavaFile);
        files = module.getSourceFiles();
        assertEquals(1, files.size());

        SourceClassInfo file = files.iterator().next();
        assertEquals(testJavaFile, file.getPath());
    }

}