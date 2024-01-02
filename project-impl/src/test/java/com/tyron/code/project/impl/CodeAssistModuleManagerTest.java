package com.tyron.code.project.impl;

import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.impl.model.RootModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeAssistModuleManagerTest {


    //language=TOML
    private static final String rootConfigString = """
            name = ""
            
            moduleType = "DEFAULT"
            includedModules = ["app"]
            """;

    //language=TOML
    private static final String appConfigString = """
            name = "app"
            moduleType = "JAVA"
            """;



    private FileManager fileManager;
    private CodeAssistModuleManager moduleManager;

    @BeforeEach
    public void setup() throws Exception {
        Path root = Files.createTempDirectory("project");
        Path rootConfig = Files.createFile(root.resolve("project.toml"));
        Files.writeString(rootConfig, rootConfigString);

        Path app = Files.createDirectory(root.resolve("app"));
        Path appConfig = Files.createFile(app.resolve("project.toml"));
        Files.writeString(appConfig, appConfigString);

        Path appSourceDir = app.resolve("src/main/java");
        Path mainJava = appSourceDir.resolve("Main.java");

        Files.createDirectories(appSourceDir);
        Files.createFile(mainJava);
        Files.writeString(mainJava, """
                class Main {
                }
                """);

        fileManager = new SimpleFileManager(root, List.of());
        moduleManager = new CodeAssistModuleManager(fileManager, root);
    }

    @Test
    public void testConfigParsing() {
        moduleManager.initialize();;

        Module module = moduleManager.getRootModule();
        assertNotNull(module);
        assertEquals(RootModuleImpl.class, module.getClass());

        RootModuleImpl rootModule = ((RootModuleImpl) module);

        List<Module> includedProjects = rootModule.getIncludedModules();
        assertNotEquals(includedProjects.size(), 0);

        Module app = includedProjects.get(0);
        assertNotNull(app);
        assertEquals("app", app.getName());

        JavaModule project = ((JavaModule) app);
        List<JavaFileInfo> files = project.getSourceFiles();
        assert !files.isEmpty();
    }
}