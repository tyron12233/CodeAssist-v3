package com.tyron.code.project.graph;

import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class CompileProjectModuleBFS extends GraphBFS<Module> {

    public CompileProjectModuleBFS(Module startingModule) {
        super(startingModule);
    }

    @Override
    protected Collection<Module> getChildren(Module node) {
        if (node instanceof JavaModule javaModule) {
            return Stream.concat(
                    Stream.of(javaModule.getJdkModule()),
                    javaModule.getCompileOnlyDependencies().stream()
            ).toList();
        }
        return Collections.emptyList();
    }
}