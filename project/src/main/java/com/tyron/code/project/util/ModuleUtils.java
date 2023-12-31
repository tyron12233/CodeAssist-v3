package com.tyron.code.project.util;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.graph.CompileProjectModuleBFS;
import com.tyron.code.project.graph.ModuleFileCollectorVisitor;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleUtils {

    public static List<Path> getCompileClassPath(Module root) {

        List<Path> files = new ArrayList<>();

        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(root);
        compileModuleBFS.traverse(currentNode -> {
            if (currentNode instanceof JarModule jarModule) {
                files.add(jarModule.getPath());
            } else if (currentNode instanceof JavaModule javaProject) {
                files.addAll(javaProject.getFiles().stream().map(SourceClassInfo::getPath).toList());
            }
        });

        return files;
    }

    public static List<ClassInfo> getFiles(String packageName, JavaModule projectModule) {
        return projectModule.getFiles().stream()
                .filter(it -> packageName.equals(it.getPackageName()))
                .map(it -> (ClassInfo) it)
                .toList();
    }

    public static List<ClassInfo> getAllClasses(JavaModule projectModule) {
        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(projectModule);
        ModuleFileCollectorVisitor visitor = new ModuleFileCollectorVisitor();
        compileModuleBFS.traverse(visitor);
        return visitor.getAllFiles();
    }

    public static List<Module> getDependenciesRecursive(JavaModule module) {
        List<Module> modules = new ArrayList<>();
        CompileProjectModuleBFS compileProjectModuleBFS = new CompileProjectModuleBFS(module);
        compileProjectModuleBFS.traverse(modules::add);
        return modules;
    }
}
