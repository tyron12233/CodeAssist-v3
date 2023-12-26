package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.PackageScope;
import com.tyron.code.project.model.module.SourceModule;

import java.nio.file.Path;
import java.util.*;

public class SourceModuleImpl extends AbstractModule implements SourceModule {

    private final PackageScope rootPackage;
    private final Map<Path, JavaFileInfo> fileMap;

    public SourceModuleImpl(Path root) {
        super(root);
        this.rootPackage = new PackageScope("");
        this.fileMap = new HashMap<>();
    }

    public void addClass(JavaFileInfo info) {
        JavaFileInfo existingFile = fileMap.get(info.path());

        addFileToPackage(info);

        if (existingFile != null) {
            removeFileFromPackage(existingFile);
        }

        fileMap.put(info.path(), info);
    }

    @Override
    public Optional<PackageScope> getPackage(List<String> qualifiers) {
        if (qualifiers.isEmpty()) {
            return Optional.of(rootPackage);
        }

        List<String> currentQualifiers = new ArrayList<>();
        PackageScope currentPackage = rootPackage;
        for (String qualifier : qualifiers) {
            Optional<PackageScope> packageScope = getPackageScope(qualifier, currentPackage);
            if (packageScope.isPresent()) {
                currentPackage = packageScope.get();
            } else {
                return Optional.empty();
            }
            currentQualifiers.add(qualifier);
        }
        return Optional.of(currentPackage);
    }

    @Override
    public Optional<JavaFileInfo> getFile(Path path) {
        return Optional.ofNullable(fileMap.get(path));
    }

    @Override
    public List<JavaFileInfo> getFiles() {
        return List.copyOf(fileMap.values());
    }

    private synchronized PackageScope getOrCreatePackage(List<String> packageQualifiers) {

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

    public void addFileToPackage(JavaFileInfo file) {
        getOrCreatePackage(file.qualifiers()).addFile(file);
    }

    public void removeFileFromPackage(JavaFileInfo file) {
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
}
