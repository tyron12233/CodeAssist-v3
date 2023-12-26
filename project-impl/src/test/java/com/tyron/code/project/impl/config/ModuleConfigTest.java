package com.tyron.code.project.impl.config;

import org.junit.jupiter.api.Test;
import red.jackf.tomlconfig.TOMLConfig;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ModuleConfigTest {

    @Test
    public void test() throws Exception {
        Path tempDirectory = Files.createTempDirectory("module");
        Path config = Files.createFile(tempDirectory.resolve("project.config"));

        TOMLConfig tomlConfig = TOMLConfig.get();
        ModuleConfig moduleConfig = tomlConfig.readConfig(ModuleConfig.class, config);
        moduleConfig.dependencies.add(new ModuleConfig.Dependency("com.tyron.test:2020:123"));

        tomlConfig.writeConfig(moduleConfig, new PrintWriter(System.out));
        System.out.println();

    }
}