package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractModule implements Module {

    private final Path root;

    private String name;
    private final List<Module> dependencies;

    public AbstractModule(Path rootDirectory) {
        this.root = rootDirectory;
        this.dependencies = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Module> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    @Override
    public Path getRootDirectory() {
        return root;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDependency(Module dependency) {
        dependencies.add(dependency);
    }

}
