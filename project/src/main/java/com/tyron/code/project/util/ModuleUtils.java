package com.tyron.code.project.util;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.graph.CompileProjectModuleBFS;
import com.tyron.code.project.graph.ModuleFileCollectorVisitor;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ModuleUtils {

    public static List<Path> getCompileClassPath(Module root) {

        List<Path> files = new ArrayList<>();

        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(root);
        compileModuleBFS.traverse(currentNode -> {
            if (currentNode instanceof JarModule jarModule) {
                files.add(jarModule.getPath());
            } else if (currentNode instanceof JavaModule javaProject) {
                files.addAll(javaProject.getSourceFiles().stream().map(SourceClassInfo::getPath).toList());
            }
        });
        if (root instanceof JavaModule java) {
            files.add(java.getJdkModule().getPath());
        }

        return files;
    }

    public static List<ClassInfo> getFiles(String packageName, JavaModule projectModule) {
        Set<Module> compileOnlyDependencies = projectModule.getCompileOnlyDependencies();
        // TODO: dependencies may expose other deps
        return Stream.concat(Stream.of(projectModule), compileOnlyDependencies.stream())
                .filter(it -> it instanceof JavaModule)
                .map(it -> (JavaModule) it)
                .flatMap(it -> it.getSourceFiles().stream())
                .filter(it -> packageName.equals(it.getPackageName()))
                .map(it -> (ClassInfo) it)
                .toList();
    }

    public static Set<ClassInfo> getAllClasses(JavaModule projectModule) {
        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(projectModule);
        ModuleFileCollectorVisitor visitor = new ModuleFileCollectorVisitor();
        compileModuleBFS.traverse(visitor);
        Set<ClassInfo> allFiles = visitor.getAllFiles();
        allFiles.addAll(projectModule.getJdkModule().getSourceFiles());
        return allFiles;
    }

    public static List<Module> getDependenciesRecursive(JavaModule module) {
        List<Module> modules = new ArrayList<>();
        CompileProjectModuleBFS compileProjectModuleBFS = new CompileProjectModuleBFS(module);
        compileProjectModuleBFS.traverse(modules::add);
        return modules;
    }
}
