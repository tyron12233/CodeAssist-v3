package com.tyron.code.project.graph;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.project.model.*;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;

import java.util.ArrayList;
import java.util.List;

public class ModuleFileCollectorVisitor implements NodeVisitor<Module> {

    private final List<ClassInfo> allFiles = new ArrayList<>();

    public ModuleFileCollectorVisitor() {

    }

    public List<ClassInfo> getAllFiles() {
        return allFiles;
    }

    @Override
    public void visit(Module module) {
        if (module instanceof JavaModule projectModule) {
            allFiles.addAll(projectModule.getFiles());
        } else if (module instanceof JarModule jarModule) {
            allFiles.addAll(jarModule.getFiles());
        }
    }
}
