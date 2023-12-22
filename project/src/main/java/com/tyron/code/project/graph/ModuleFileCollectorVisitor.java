package com.tyron.code.project.graph;

import com.tyron.code.project.model.JarModule;
import com.tyron.code.project.model.Module;
import com.tyron.code.project.model.ProjectModule;
import com.tyron.code.project.model.UnparsedJavaFile;

import java.util.ArrayList;
import java.util.List;

public class ModuleFileCollectorVisitor implements NodeVisitor<Module> {

    private final List<UnparsedJavaFile> allFiles = new ArrayList<>();

    public ModuleFileCollectorVisitor() {

    }

    public List<UnparsedJavaFile> getAllFiles() {
        return allFiles;
    }

    @Override
    public void visit(Module module) {
        if (module instanceof ProjectModule projectModule) {
            allFiles.addAll(projectModule.getFiles());
        } else if (module instanceof JarModule jarModule) {
            allFiles.addAll(jarModule.getClasses());
        }
    }
}
