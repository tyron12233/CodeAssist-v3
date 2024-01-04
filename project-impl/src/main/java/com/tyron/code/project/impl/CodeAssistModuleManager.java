package com.tyron.code.project.impl;

import com.tyron.code.logging.Logging;
import com.tyron.code.project.InitializationException;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.graph.GraphBFS;
import com.tyron.code.project.graph.NodeVisitor;
import com.tyron.code.project.impl.model.RootModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.ProjectError;
import com.tyron.code.project.model.module.ErroneousRootModule;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses CodeAssist's projects, handling project dependency and others
 */
public class CodeAssistModuleManager implements ModuleManager {

    private static final Logger logger = Logging.get(CodeAssistModuleManager.class);

    private final FileManager fileManager;
    private final Path rootDirectory;

    private final Map<String, Module> includedProjects;

    private RootModuleImpl rootProject;

    public CodeAssistModuleManager(FileManager fileManager, Path rootDirectory) {
        this.fileManager = fileManager;
        this.rootDirectory = rootDirectory;
        includedProjects = new HashMap<>();
    }

    @Override
    public void initialize() {
        ProjectStructureParser parser = new ProjectStructureParser(this);
        rootProject = parser.parse(rootDirectory);

        Set<Module> modulesParsed = parser.getModulesParsed();
        for (Module module : modulesParsed) {
            includedProjects.put(module.getName(), module);
        }

        ModuleInitializer initializer = new ModuleInitializer();
        initializer.initializeModules(
                Stream.concat(
                        Stream.of(rootProject),
                        rootProject.getIncludedModules().stream()
                ).toList()
        );
    }

    @Override
    public Optional<Module> findModuleByName(String name) {
        return Optional.ofNullable(includedProjects.get(name));
    }

    @Override
    public Optional<JavaFileInfo> getFileItem(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RootModule getRootModule() {
        return rootProject;
    }

    @Override
    public Optional<Module> findModuleByFile(Path file) {
        List<Module> includedModules = getRootModule().getIncludedModules();
        for (Module module : includedModules) {
            Path moduleRoot = module.getRootDirectory();
            if (file.startsWith(moduleRoot)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }
}
