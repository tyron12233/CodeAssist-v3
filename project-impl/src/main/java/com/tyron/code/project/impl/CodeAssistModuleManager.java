package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableList;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.impl.model.RootModule;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.Module;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Parses CodeAssist's projects, handling project dependency and others
 */
public class CodeAssistModuleManager implements ModuleManager {


    private final FileManager fileManager;
    private final Path rootDirectory;

    private final Map<String, Module> includedProjects;

    private RootModule rootProject;

    public CodeAssistModuleManager(FileManager fileManager, Path rootDirectory) {
        this.fileManager = fileManager;
        this.rootDirectory = rootDirectory;
        includedProjects = new HashMap<>();
    }

    @Override
    public void initialize() {
        ProjectStructureParser parser = new ProjectStructureParser();
        rootProject = parser.parse(rootDirectory);

        ModuleInitializer initializer = new ModuleInitializer();
        initializer.initializeModules(
                Stream.concat(
                        Stream.of(rootProject),
                        rootProject.getIncludedModules().stream()
                ).toList()
        );
    }


    @Override
    public Optional<JavaFileInfo> getFileItem(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addOrUpdateFile(Path path) {

    }

    @Override
    public void removeFile(Path path) {

    }

    @Override
    public void addDependingModule(Module module) {

    }

    public List<Module> getIncludedProjects() {
        return ImmutableList.copyOf(includedProjects.values());
    }

    @Override
    public Module getRootModule() {
        return rootProject;
    }
}
