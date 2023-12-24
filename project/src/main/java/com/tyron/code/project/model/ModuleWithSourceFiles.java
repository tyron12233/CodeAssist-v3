package com.tyron.code.project.model;

import java.util.*;

public class ModuleWithSourceFiles extends Module {

    private PackageScope rootPackage;

    public ModuleWithSourceFiles(ModuleType moduleType) {
        this(moduleType, "");
    }

    public ModuleWithSourceFiles(ModuleType moduleType, String debugName) {
        super(moduleType, debugName);

        rootPackage = new PackageScope("");
    }

    public void addClass(UnparsedJavaFile unparsedJavaFile) {
        getOrCreatePackage(unparsedJavaFile.qualifiers()).addFile(unparsedJavaFile);
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

    public Optional<PackageScope> getPackageScope(String name, PackageScope packageScope) {
        List<PackageScope> subPackages = packageScope.getSubPackages(name);
        if (subPackages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(subPackages.get(0));
    }

    public void addFileToPackage(UnparsedJavaFile file) {
        getOrCreatePackage(file.qualifiers()).addFile(file);
    }

    public void removeFileFromPackage(UnparsedJavaFile file) {
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
