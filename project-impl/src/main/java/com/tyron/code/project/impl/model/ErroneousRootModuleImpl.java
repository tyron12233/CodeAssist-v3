package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.ProjectError;
import com.tyron.code.project.model.module.ErroneousRootModule;
import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.List;

public class ErroneousRootModuleImpl extends RootModuleImpl implements ErroneousRootModule {

    private final List<ProjectError> errors;

    public ErroneousRootModuleImpl(Path rootDirectory, List<Module> includedModules, List<ProjectError> errors) {
        super(rootDirectory, includedModules);
        this.errors = errors;
    }

    @Override
    public List<ProjectError> getErrors() {
        return errors;
    }
}
