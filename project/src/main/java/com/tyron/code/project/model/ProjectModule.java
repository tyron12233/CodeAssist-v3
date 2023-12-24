package com.tyron.code.project.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProjectModule extends ModuleWithSourceFiles {

    private final Map<Path, UnparsedJavaFile> fileMap;

    private final List<Module> compileTimeDependencies;
    private final List<Module> runtimeDependencies;
    private final String projectName;
    private Path directory;

    public ProjectModule(String projectName) {
        super(ModuleType.PROJECT, projectName);
        this.projectName = projectName;
        this.fileMap = new HashMap<>();
        this.compileTimeDependencies = new ArrayList<>();
        this.runtimeDependencies = new ArrayList<>();
    }

    public void addImplementationDependency(Module module) {
        compileTimeDependencies.add(module);
        runtimeDependencies.add(module);
    }

    public List<Module> getDependingModules(DependencyType type) {
        return switch (type) {
            case RUNTIME -> runtimeDependencies;
            case COMPILE_TIME -> compileTimeDependencies;
        };
    }

    public void addOrReplaceFile(UnparsedJavaFile unparsedJavaFile) {
        UnparsedJavaFile existingFile = fileMap.get(unparsedJavaFile.path());

        addFileToPackage(unparsedJavaFile);

        if (existingFile != null) {
            removeFileFromPackage(existingFile);
        }

        fileMap.put(unparsedJavaFile.path(), unparsedJavaFile);
    }


    public synchronized Optional<UnparsedJavaFile> getFile(String pathString) {
        return Optional.ofNullable(fileMap.get(Paths.get(pathString)));
    }

    public List<UnparsedJavaFile> getFiles() {
        return ImmutableList.copyOf(fileMap.values());
    }

    public String getProjectName() {
        return projectName;
    }

    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }

    public JarModule getJdkModule() {
        return compileTimeDependencies.stream().filter(module -> module.getModuleType() == ModuleType.JDK)
                .findFirst()
                .map(it -> (JarModule) it)
                .orElseThrow();
    }

    public void addJdkDependency(JarModule jarModule) {
        this.compileTimeDependencies.add(jarModule);
    }
}
