package com.tyron.code.project.util;

import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.graph.CompileProjectModuleBFS;
import com.tyron.code.project.graph.ModuleFileCollectorVisitor;
import com.tyron.code.project.model.*;
import com.tyron.code.project.model.Module;

import java.nio.file.Path;
import java.util.*;

public class ModuleUtils {

    public static List<Path> getCompileClassPath(Module root) {

        List<Path> files = new ArrayList<>();

        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(root);
        compileModuleBFS.traverse(currentNode -> {
            switch (currentNode.getModuleType()) {
                case JAR_DEPENDENCY -> files.add(((JarModule) currentNode).getJarPath());
                case PROJECT -> files.addAll(((ProjectModule) currentNode).getFiles().stream().map(UnparsedJavaFile::path).toList());
            }
        });

        return files;
    }

    public static List<UnparsedJavaFile> getFiles(List<String> qualifiers, ProjectModule projectModule) {
        return projectModule.getFiles().stream().filter(it -> it.qualifiers().equals(qualifiers)).toList();
    }

    public static List<UnparsedJavaFile> getAllClasses(ProjectModule projectModule) {
        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(projectModule);
        ModuleFileCollectorVisitor visitor = new ModuleFileCollectorVisitor();
        compileModuleBFS.traverse(visitor);
        return visitor.getAllFiles();
    }
}
