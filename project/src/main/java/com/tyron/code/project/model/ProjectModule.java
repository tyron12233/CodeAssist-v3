package com.tyron.code.project.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProjectModule extends Module {

    private final Map<Path, UnparsedJavaFile> fileMap;

    private final PackageScope rootPackage;

    private final List<Module> compileTimeDependencies;
    private final List<Module> runtimeDependencies;
    private final String projectName;
    private Path directory;

    public ProjectModule(String projectName) {
        super(ModuleType.PROJECT, projectName);
        this.projectName = projectName;
        this.fileMap = new HashMap<>();
        this.rootPackage = new PackageScope("");
        this.compileTimeDependencies = new ArrayList<>();
        this.runtimeDependencies = new ArrayList<>();
    }

    public synchronized PackageScope getOrCreatePackage(List<String> packageQualifiers) {

        // root package
        if (packageQualifiers.isEmpty()) {
            return rootPackage;
        }

        List<String> currentQualifiers = new ArrayList<>();
        PackageScope currentPackage = rootPackage;
        for (String qualifier : packageQualifiers) {
            Optional<PackageScope> packageScope = getPackageScope(qualifier, currentPackage);
            if (packageScope.isPresent()) {
                currentPackage = packageScope.get();
            } else {
                PackageScope newPackageScope = new PackageScope(currentPackage, qualifier);
                currentPackage.addPackage(newPackageScope);
                currentPackage = newPackageScope;
            }
            currentQualifiers.add(qualifier);
        }
        return currentPackage;
    }

    private Optional<PackageScope> getPackageScope(String name, PackageScope packageScope) {
        List<PackageScope> subPackages = packageScope.getSubPackages(name);
        if (subPackages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subPackages.get(0));
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

    private void addFileToPackage(UnparsedJavaFile file) {
        getOrCreatePackage(file.qualifiers()).addFile(file);
    }

    private void removeFileFromPackage(UnparsedJavaFile file) {
        Deque<PackageScope> stack = new ArrayDeque<>();
        PackageScope currentPackage = rootPackage;
        for (String qualifier : file.qualifiers()) {
            Optional<PackageScope> packageScope = getPackageScope(qualifier, currentPackage);
            PackageScope scope = packageScope.orElseThrow();
            stack.addFirst(scope);
            currentPackage = scope;
        }
        currentPackage.removeFile(file);
        while (!currentPackage.hasChildren() && !stack.isEmpty()) {
            PackageScope scope = stack.removeFirst();
            currentPackage = stack.isEmpty() ? rootPackage : stack.peekFirst();
            currentPackage.removePackage(scope);
        }
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
