package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.util.PathUtils;
import com.tyron.code.project.util.StringSearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModuleInitializer {

    public void initializeModules(List<Module> modules) {
        List<IOException> exceptions = new ArrayList<>();
        for (Module module : modules) {
            try {
                if (module instanceof JavaModuleImpl project) {
                    initializeJavaProject(project);
                    continue;
                }

                if (module instanceof JarModuleImpl jarModule) {
                    initializeJarModule(jarModule);
                    continue;
                }
            } catch (IOException e) {
                exceptions.add(e);
            }
        }
    }

    private void initializeJavaProject(JavaModuleImpl project) {
        if (!Files.exists(project.getSourceDirectory())) {
            return;
        }
        ImmutableMap<String, Consumer<Path>> handlers =
                ImmutableMap.of(
                        ".java",
                        path -> addOrUpdateFile(project, path)
                );
        PathUtils.walkDirectory(project.getSourceDirectory(), handlers, it -> false);
    }

    private void addOrUpdateFile(JavaModuleImpl module, Path path) {
        String name = path.getFileName().toString();
        if (name.endsWith(".java")) {
            name = name.substring(0, name.length() - ".java".length());
        }
        List<String> qualifiers = Arrays.stream(StringSearch.packageName(path).split("\\.")).toList();

        if (qualifiers.size() == 1 && qualifiers.get(0).isEmpty()) {
            qualifiers = Collections.emptyList();
        }

        JavaFileInfo javaFileInfo = new JavaFileInfo(module, path, name, qualifiers);
        module.addClass(javaFileInfo);
    }

    private void initializeJarModule(JarModuleImpl jarModule) throws IOException {
        List<JarReader.ClassInfo> infos = JarReader.readJarFile(jarModule.getPath());
        infos.stream()
                .map(it -> new JavaFileInfo(jarModule, it.classPath(), it.className(), it.packageQualifiers()))
                .forEach(jarModule::addClass);
    }
}
