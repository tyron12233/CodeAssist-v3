package com.tyron.code.project.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageScope {

    private final Multimap<String, PackageScope> subPackages;

    private final Set<JavaFileInfo> files;

    private final String simpleName;

    private final PackageScope parent;

    public PackageScope(String simpleName) {
        this(null, simpleName);
    }

    public PackageScope(PackageScope parent, String simpleName) {
        this.parent = parent;
        this.simpleName = simpleName;
        this.subPackages = ArrayListMultimap.create();
        this.files = new HashSet<>();

    }

    public List<PackageScope> getSubPackages(String name) {
        return ImmutableList.copyOf(subPackages.get(name));
    }

    public List<PackageScope> getSubPackages() {
        return ImmutableList.copyOf(subPackages.values());
    }

    public void addPackage(PackageScope newPackageScope) {
        subPackages.put(newPackageScope.getSimpleName(), newPackageScope);
    }

    public void removePackage(PackageScope packageScope) {
        subPackages.remove(packageScope.getSimpleName(), packageScope);
    }

    public String getSimpleName() {
        return this.simpleName;
    }

    public Set<JavaFileInfo> getFiles() {
        return files;
    }

    public void addFile(JavaFileInfo file) {
        files.add(file);
    }

    public PackageScope getParent() {
        return parent;
    }

    public void removeFile(JavaFileInfo file) {
        files.remove(file);
    }

    public boolean hasChildren() {
        return !(subPackages.isEmpty() && files.isEmpty());
    }
}
