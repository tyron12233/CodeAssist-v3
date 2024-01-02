package com.tyron.code.project.graph;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;

import java.util.HashSet;
import java.util.Set;

public class ModuleFileCollectorVisitor implements NodeVisitor<Module> {

    private final Set<ClassInfo> allFiles = new HashSet<>();

    public ModuleFileCollectorVisitor() {

    }

    public Set<ClassInfo> getAllFiles() {
        return allFiles;
    }

    @Override
    public void visit(Module module) {
        if (module instanceof JavaModule projectModule) {
            allFiles.addAll(projectModule.getSourceFiles());
        } else if (module instanceof JarModule jarModule) {
            allFiles.addAll(jarModule.getSourceFiles());
        }
    }
}
