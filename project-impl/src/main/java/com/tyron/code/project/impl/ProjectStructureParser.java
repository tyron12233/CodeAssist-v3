package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableList;
import com.tyron.code.logging.Logging;
import com.tyron.code.project.InitializationException;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.impl.config.ModuleConfig;
import com.tyron.code.project.impl.model.*;
import com.tyron.code.project.model.ProjectError;
import com.tyron.code.project.model.module.Module;
import org.slf4j.Logger;
import red.jackf.tomlconfig.TOMLConfig;
import red.jackf.tomlconfig.settings.FailMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProjectStructureParser {

    private static final Logger logger = Logging.get(ProjectStructureParser.class);

    private static final TOMLConfig TOML_CONFIG = TOMLConfig.builder()
            .withReadFailMode(FailMode.THROW)
            .build();

    private final Set<Module> modulesParsed = new HashSet<>();
    private final Map<String, Module> includedProjects;
    private final Map<Module, ModuleConfig> configMap;

    private final List<ProjectError> rootErrors = new ArrayList<>();
    private final ModuleManager moduleManager;


    public ProjectStructureParser(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        includedProjects = new HashMap<>();
        configMap = new HashMap<>();
    }

    public Set<Module> getModulesParsed() {
        return modulesParsed;
    }

    public RootModuleImpl parse(Path rootDirectory) {
        RootModuleImpl rootModule = parseImpl(rootDirectory);
        modulesParsed.add(rootModule);
        return rootModule;
    }

    private RootModuleImpl parseImpl(Path rootDirectory) {
        logger.debug("Parsing project structure at: " + rootDirectory);

        Path rootConfig = rootDirectory.resolve(Module.CONFIG_NAME);
        if (!Files.exists(rootConfig)) {
            rootErrors.add(new ProjectError("Root configuration does not exist."));
            return new ErroneousRootModuleImpl(moduleManager, rootDirectory, List.of(), rootErrors);
        }

        ModuleConfig rootModuleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, rootConfig);

        if (rootModuleConfig.moduleType == ModuleConfig.ModuleType.JAVA) {
            rootErrors.add(new ProjectError("JAVA type is not supported on root projects."));
            return new ErroneousRootModuleImpl(moduleManager, rootDirectory, List.of(), rootErrors);
        }

        List<Path> includedProjectPaths = rootModuleConfig.includedModules.stream().map(rootDirectory::resolve).toList();
        includedProjectPaths.stream().filter(path -> !Files.exists(path))
                .forEach(nonExistent -> rootErrors.add(new ProjectError("Included path does not exist: " + nonExistent)));

        logger.debug("Found " + includedProjectPaths.size() + " included project(s).");

        List<Path> existingProjectPaths = includedProjectPaths.stream()
                .filter(Files::exists)
                .toList();
        for (Path existingProjectPath : existingProjectPaths) {
            processIncludedProject(existingProjectPath);
        }

        resolveProjectDependencies();

        if (!rootErrors.isEmpty()) {
            return new ErroneousRootModuleImpl(moduleManager, rootDirectory, ImmutableList.copyOf(includedProjects.values()), rootErrors);
        }

        return new RootModuleImpl(moduleManager, rootDirectory, ImmutableList.copyOf(includedProjects.values()));
    }

    private void processIncludedProject(Path existingProjectPath) {
        AbstractModule module;
        try {
            module = processIncludedProjectImpl(existingProjectPath);
        } catch (InitializationException e) {
            module = new ErroneousModuleImpl(moduleManager, existingProjectPath, List.of(new ProjectError(e.getMessage())));
        }
        Module old = includedProjects.put(module.getName(), module);
        if (old != null) {
            throw new InitializationException("Duplicate module name: " + module.getName());
        }
        modulesParsed.add(module);
    }

    private AbstractModule processIncludedProjectImpl(Path path) throws InitializationException {
        Path configPath = path.resolve(Module.CONFIG_NAME);
        if (!Files.exists(configPath)) {
            throw new InitializationException("Included project does not have a configuration file: " + configPath);
        }

        ModuleConfig moduleConfig;
        try {
             moduleConfig = TOML_CONFIG.readConfig(ModuleConfig.class, configPath);
        } catch (RuntimeException e) {
            throw new InitializationException(e.getCause().getMessage());
        }
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.DEFAULT) {
            throw new InitializationException("Included project does not have a module type: " + configPath);
        }

        final AbstractModule module;
        if (moduleConfig.moduleType == ModuleConfig.ModuleType.JAVA) {
            module = new JavaModuleImpl(moduleManager, path);
        } else {
            throw new InitializationException("Unsupported module type: " + moduleConfig.moduleType);
        }


        String name = moduleConfig.name.isEmpty() ? path.getFileName().toString() : moduleConfig.name;
        module.setName(name);

        configMap.put(module, moduleConfig);
        return module;
    }

    private void resolveProjectDependencies() {
        includedProjects.values().forEach(includedProject -> {
            ModuleConfig moduleConfig = configMap.get(includedProject);
            if (moduleConfig == null) {
                rootErrors.add(new ProjectError("Module " + includedProject.getName() + " was included but no configuration was parsed."));
                return;
            }

            logger.debug("Resolving dependencies for " + includedProject.getName());
            List<ModuleConfig.Dependency> dependencies = moduleConfig.dependencies;
            dependencies.forEach(dependency -> handleDependency(includedProject, dependency));

            logger.debug("Finished resolving dependencies for " + includedProject.getName());

            if (includedProject instanceof JavaModuleImpl javaModule) {
                logger.debug("Using default JDK");
                Path path = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/completions/src/test/resources/android.jar");
                JdkModuleImpl jdkModule = new JdkModuleImpl(moduleManager, path, "11");
                javaModule.setJdk(jdkModule);
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
            rootErrors.add(new ProjectError("Absolute paths are not supported. Notation: " + notation));
            return;
        }
        Path path = Paths.get(notation);
        Path resolvedPath = includedProject.getRootDirectory().resolve(path);
        if (!Files.exists(resolvedPath)) {
            rootErrors.add(new ProjectError("File dependency does not exist at: " + resolvedPath));
            return;
        }

        JarModuleImpl jarModule = new JarModuleImpl(moduleManager, resolvedPath);
        modulesParsed.add(jarModule);
        addDependencyWithScope(((JavaModuleImpl) includedProject), jarModule, dependency.scope);

    }

    private void handleMavenDependency(Module indcludedProject, ModuleConfig.Dependency dependency) {
        rootErrors.add(new ProjectError("Maven libraries are not yet supported."));
    }

    private void handleProjectDependency(Module includedProject, ModuleConfig.Dependency dependency) {
        String notation = dependency.notation;
        Module module = includedProjects.get(notation);
        if (module == null) {
            rootErrors.add(new ProjectError("Unable to find module " + notation));
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
