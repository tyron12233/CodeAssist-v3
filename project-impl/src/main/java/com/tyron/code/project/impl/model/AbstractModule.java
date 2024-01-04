package com.tyron.code.project.impl.model;

import com.tyron.code.info.FileInfo;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AbstractModule implements Module {

    private final ModuleManager moduleManager;
    private final Path root;

    private String name;
    private final List<String> dependencies;

    private final List<FileInfo> files;

    public AbstractModule(ModuleManager moduleManager, Path rootDirectory) {
        this.moduleManager = moduleManager;
        this.root = rootDirectory;
        this.dependencies = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    @Override
    public @NotNull ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public @NotNull Path getRootDirectory() {
        return root;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDependency(Module dependency) {
        dependencies.add(dependency.getName());
    }

    @Override
    public @NotNull List<FileInfo> getFiles() {
        return files;
    }

    public void addFile(FileInfo file) {
        files.add(file);
    }

    public void addFiles(List<FileInfo> files) {
        this.files.addAll(files);
    }
}
