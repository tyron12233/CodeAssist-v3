package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableList;
import com.tyron.code.project.impl.config.ModuleConfig;
import com.tyron.code.project.impl.model.AbstractModule;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.RootModule;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.Module;
import red.jackf.tomlconfig.TOMLConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectStructureParser {

    private static final String PROJECT_CONFIG_NAME = "project.toml";

    private static final TOMLConfig TOML_CONFIG = TOMLConfig.get();

    private final Map<String, Module> includedProjects;
    private final Map<Module, ModuleConfig> configMap;

    private final List<Error> errors;

    public ProjectStructureParser() {
        includedProjects = new HashMap<>();
        errors = new ArrayList<>();
        configMap = new HashMap<>();
    }

    public RootModule parse(Path rootDirectory) {
        Path rootConfig = rootDirectory.resolve(PROJECT_CONFIG_NAME);
        if (!Files.exists(rootConfig)) {
            errors.add(new Error("No project config found in root directory."));
        }

        ModuleConfig rootModuleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, rootConfig);

        List<Path> includedProjectPaths = rootModuleConfig.includedModules.stream().map(rootDirectory::resolve).toList();
        includedProjectPaths.stream().filter(path -> !Files.exists(path))
                .forEach(nonExistent -> errors.add(new Error("Included path does not exisit: " + nonExistent)));

        List<Path> existingProjectPaths = includedProjectPaths.stream()
                .filter(Files::exists)
                .toList();
        existingProjectPaths.forEach(this::processIncludedProject);

        resolveProjectDependencies();

        return new RootModule(rootDirectory, ImmutableList.copyOf(includedProjects.values()));
    }

    private void processIncludedProject(Path path) {
        Path configPath = path.resolve(PROJECT_CONFIG_NAME);
        if (!Files.exists(configPath)) {
            errors.add(new Error("Configuration " + path + " does not exist."));
            return;
        }

        ModuleConfig moduleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, configPath);
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.DEFAULT) {
            errors.add(new Error("DEFAULT type is not supported on child projects."));
            return;
        }

        AbstractModule module = null;
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.JAVA) {
            module = new JavaModuleImpl(path);
        }

        if (module == null) {
            errors.add(new Error("Unsupported project: " + path));
            return;
        }

        String name = moduleConfig.name.isEmpty() ? path.getFileName().toString() : moduleConfig.name;
        module.setName(name);

        includedProjects.put(name, module);
        configMap.put(module, moduleConfig);
    }

    private void resolveProjectDependencies() {
        for (Module includedProject : includedProjects.values()) {
            ModuleConfig moduleConfig = configMap.get(includedProject);
            if (moduleConfig == null) {
                errors.add(new Error("Module " + includedProject.getName() + " was included but no configuration was parsed."));
                continue;
            }

            List<ModuleConfig.Dependency> dependencies = moduleConfig.dependencies;
            for (ModuleConfig.Dependency dependency : dependencies) {
                handleDependency(includedProject, dependency);
            }
        }
    }

    private void handleDependency(Module includedProject, ModuleConfig.Dependency dependency) {
        switch (dependency.type) {
            case PROJECT -> handleProjectDependency(includedProject, dependency);
            case MAVEN -> handleMavenDependency(includedProject, dependency);
            case FILE -> handleFileDependency(includedProject, dependency);
        }
    }

    private void handleFileDependency(Module includedProject, ModuleConfig.Dependency dependency) {
        String notation = dependency.notation;
        if (notation.startsWith("/")) {
            errors.add(new Error("Absolute paths are not supported. Notation: " + notation));
            return;
        }
        Path path = Paths.get(notation);
        Path resolvedPath = includedProject.getRootDirectory().resolve(path);
        if (!Files.exists(resolvedPath)) {
            errors.add(new Error("File dependency does not exist at: " + resolvedPath));
            return;
        }

        try {
            JarModule jarModule = JarReader.toJarModule(resolvedPath);
            addDependencyWithScope(((JavaModuleImpl) includedProject), jarModule, dependency.scope);
        } catch (IOException e) {
            errors.add(new Error("Exception while reading dependency " + notation + "\nMessage: " + e.getMessage()));
        }

    }

    private void handleMavenDependency(Module indcludedProject, ModuleConfig.Dependency dependency) {
        errors.add(new Error("Maven libraries are not yet supported."));
    }

    private void handleProjectDependency(Module includedProject, ModuleConfig.Dependency dependency) {
        String notation = dependency.notation;
        Module module = includedProjects.get(notation);
        if (module == null) {
            errors.add(new Error("Unable to find module " + notation));
            return;
        }

        JavaModuleImpl javaModule = (JavaModuleImpl) includedProject;
        addDependencyWithScope(javaModule, module, dependency.scope);
    }

    private void addDependencyWithScope(JavaModuleImpl depending, Module to, ModuleConfig.Dependency.DependencyScope scope) {
        switch (scope) {
            case COMPILE_ONLY -> depending.addCompileOnly(to);
            case RUNTIME_ONLY -> depending.addRuntimeOnly(to);
            case IMPLEMENTATION -> depending.addImplementationDependency(to);
        }

    }


    public record Error(String message) {
    }
}
