package com.tyron.code.project;

import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.model.DependencyType;
import com.tyron.code.project.model.JarModule;
import com.tyron.code.project.model.Module;
import com.tyron.code.project.model.ProjectModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeAssistModuleManagerTest {


    //language=TOML
    private static final String rootConfigString = """
            [build]
            
            [settings]
            include = [
              'app'
            ]
            projectName = "root"
            """;

    //language=TOML
    private static final String appConfigString = """
            [build]
            
            
            [settings]
            projectName = "app"
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

        fileManager = new SimpleFileManager(root, List.of());
        moduleManager = new CodeAssistModuleManager(fileManager, root);
    }

    @Test
    public void testConfigParsing() {
        moduleManager.initialize();;

        Module rootModule = moduleManager.getRootModule();
        assertNotNull(rootModule);
        assertSame(rootModule.getClass(), ProjectModule.class);

        ProjectModule rootProjectModule = (ProjectModule) rootModule;
        assertEquals(rootProjectModule.getDebugName(), "root");
        List<Module> dependingModules = rootProjectModule.getDependingModules(DependencyType.COMPILE_TIME);
        assertEquals(dependingModules.size(), 0);

        List<ProjectModule> includedProjects = moduleManager.getIncludedProjects();
        assertNotEquals(includedProjects.size(), 0);

        Module module = includedProjects.get(0);
        assertNotNull(module);
        assertEquals(module.getClass(), ProjectModule.class);
        assertEquals(module.getDebugName(), "app");
    }
}