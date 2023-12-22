package com.tyron.code.project.graph;

import com.tyron.code.project.model.DependencyType;
import com.tyron.code.project.model.Module;
import com.tyron.code.project.model.ProjectModule;

import java.util.Collections;
import java.util.List;

public class CompileProjectModuleBFS extends GraphBFS<Module> {

    public CompileProjectModuleBFS(Module startingModule) {
        super(startingModule);
    }

    @Override
    protected List<Module> getChildren(Module node) {
        if (node instanceof ProjectModule projectModule) {
            return projectModule.getDependingModules(DependencyType.COMPILE_TIME);
        }
        return Collections.emptyList();
    }
}