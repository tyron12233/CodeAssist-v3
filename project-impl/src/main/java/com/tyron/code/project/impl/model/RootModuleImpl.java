package com.tyron.code.project.impl.model;

import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;

import java.nio.file.Path;
import java.util.List;

public class RootModuleImpl extends AbstractModule implements RootModule {

    private final List<Module> includedModules;

    public RootModuleImpl(ModuleManager moduleManager, Path rootDirectory, List<Module> includedModules) {
        super(moduleManager, rootDirectory);
        this.includedModules = includedModules;
        setName(rootDirectory.getFileName().toString());
    }

    public List<Module> getIncludedModules() {
        return includedModules;
    }
}
