package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.List;

public class RootModule extends AbstractModule {

    private final List<Module> includedModules;

    public RootModule(Path rootDirectory, List<Module> includedModules) {
        super(rootDirectory);
        this.includedModules = includedModules;
    }

    public List<Module> getIncludedModules() {
        return includedModules;
    }
}
