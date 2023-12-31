package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableList;
import com.tyron.code.logging.Logging;
import com.tyron.code.project.impl.config.ModuleConfig;
import com.tyron.code.project.impl.model.*;
import com.tyron.code.project.model.ProjectError;
import com.tyron.code.project.model.module.ErroneousRootModule;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.Module;
import org.slf4j.Logger;
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

    private static final Logger logger = Logging.get(ProjectStructureParser.class);

    private static final String PROJECT_CONFIG_NAME = "project.toml";

    private static final TOMLConfig TOML_CONFIG = TOMLConfig.get();

    private final Map<String, Module> includedProjects;
    private final Map<Module, ModuleConfig> configMap;

    private final List<ProjectError> errors;

    public ProjectStructureParser() {
        includedProjects = new HashMap<>();
        errors = new ArrayList<>();
        configMap = new HashMap<>();
    }

    public List<ProjectError> getErrors() {
        return errors;
    }

    public RootModuleImpl parse(Path rootDirectory) {
        try {
            return parseImpl(rootDirectory);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                errors.add(new ProjectError(ioException.getClass().getName() + ": " + ioException.getMessage()));
            }

            return new ErroneousRootModuleImpl(rootDirectory, errors);
        }
    }

    private RootModuleImpl parseImpl(Path rootDirectory) {
        logger.debug("Parsing project structure at: " + rootDirectory);

        Path rootConfig = rootDirectory.resolve(PROJECT_CONFIG_NAME);
        ModuleConfig rootModuleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, rootConfig);

        List<Path> includedProjectPaths = rootModuleConfig.includedModules.stream().map(rootDirectory::resolve).toList();
        includedProjectPaths.stream().filter(path -> !Files.exists(path))
                .forEach(nonExistent -> errors.add(new ProjectError("Included path does not exisit: " + nonExistent)));

        logger.debug("Found " + includedProjectPaths.size() + " included project(s).");

        List<Path> existingProjectPaths = includedProjectPaths.stream()
                .filter(Files::exists)
                .toList();
        existingProjectPaths.forEach(this::processIncludedProject);

        resolveProjectDependencies();

        return new RootModuleImpl(rootDirectory, ImmutableList.copyOf(includedProjects.values()));
    }

    private void processIncludedProject(Path path) {
        Path configPath = path.resolve(PROJECT_CONFIG_NAME);
        if (!Files.exists(configPath)) {
            errors.add(new ProjectError("Configuration " + path + " does not exist."));
            return;
        }

        ModuleConfig moduleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, configPath);
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.DEFAULT) {
            errors.add(new ProjectError("DEFAULT type is not supported on child projects."));
            return;
        }

        AbstractModule module = null;
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.JAVA) {
            module = new JavaModuleImpl(path);
        }

        if (module == null) {
            errors.add(new ProjectError("Unsupported project: " + path));
            return;
        }

        String name = moduleConfig.name.isEmpty() ? path.getFileName().toString() : moduleConfig.name;
        module.setName(name);

        includedProjects.put(name, module);
        configMap.put(module, moduleConfig);
    }

    private void resolveProjectDependencies() {
        includedProjects.values().forEach(includedProject -> {
            ModuleConfig moduleConfig = configMap.get(includedProject);
            if (moduleConfig == null) {
                errors.add(new ProjectError("Module " + includedProject.getName() + " was included but no configuration was parsed."));
                return;
            }

            logger.debug("Resolving dependencies for " + includedProject.getName());
            List<ModuleConfig.Dependency> dependencies = moduleConfig.dependencies;
            dependencies.forEach(dependency -> handleDependency(includedProject, dependency));

            logger.debug("Finished resolving dependencies for " + includedProject.getName());

            if (includedProject instanceof JavaModuleImpl javaModule) {
                logger.debug("Using default JDK");
                Path path = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/completions/src/test/resources/android.jar");
                javaModule.setJdk(new JdkModuleImpl(path, "11"));
            }
        });
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
            errors.add(new ProjectError("Absolute paths are not supported. Notation: " + notation));
            return;
        }
        Path path = Paths.get(notation);
        Path resolvedPath = includedProject.getRootDirectory().resolve(path);
        if (!Files.exists(resolvedPath)) {
            errors.add(new ProjectError("File dependency does not exist at: " + resolvedPath));
            return;
        }

        JarModuleImpl jarModule = new JarModuleImpl(resolvedPath);
        new ModuleInitializer().initializeModule(jarModule);
        addDependencyWithScope(((JavaModuleImpl) includedProject), jarModule, dependency.scope);

    }

    private void handleMavenDependency(Module indcludedProject, ModuleConfig.Dependency dependency) {
        errors.add(new ProjectError("Maven libraries are not yet supported."));
    }

    private void handleProjectDependency(Module includedProject, ModuleConfig.Dependency dependency) {
        String notation = dependency.notation;
        Module module = includedProjects.get(notation);
        if (module == null) {
            errors.add(new ProjectError("Unable to find module " + notation));
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
}
