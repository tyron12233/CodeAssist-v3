package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.info.JvmClassInfo;
import com.tyron.code.info.builder.SourceClassInfoBuilder;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.RootModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;
import com.tyron.code.project.util.PathUtils;
import com.tyron.code.project.util.StringSearch;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * A simple single project module manager which adds all jars found in the root path
 * as a dependency
 */
public class FileSystemModuleManager implements ModuleManager {

    private static final String JAVA_EXTENSION = ".java";
    private static final String JAR_EXTENSION = ".jar";
    private final Path root;

    private final RootModule projectModule;
    private final JavaModuleImpl javaModule;

    private final FileManager fileManager;

    public FileSystemModuleManager(FileManager fileManager, Path root) {
        this.fileManager = fileManager;
        this.javaModule = new JavaModuleImpl(this, root);
        this.projectModule = new RootModuleImpl(this, root, List.of(javaModule));
        this.root = root;
    }

    @Override
    public RootModule getRootModule() {
        return projectModule;
    }

    @Override
    public void initialize() {
        walkDirectory(root);
    }

    @Override
    public synchronized Optional<JavaFileInfo> getFileItem(Path path) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addOrUpdateFile(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFile(Path path) {

    }

    @Override
    public void addDependingModule(Module module) {
        javaModule.addImplementationDependency(module);
    }

    private void walkDirectory(Path root) {
        ImmutableMap<String, Consumer<Path>> handlers =
                ImmutableMap.of(
                        JAVA_EXTENSION,
                        path -> addOrUpdateFile(javaModule, path),
                        JAR_EXTENSION,
                        this::addJarModule
                );

        PathUtils.walkDirectory(root, handlers, fileManager::shouldIgnorePath);
    }

    private void addJarModule(Path path) {
        try {
            JarModuleImpl jarModule = new JarModuleImpl(this, path);
            Path rootJarPath = PathUtils.getRootPathForJarFile(path);


            List<JvmClassInfo> jvmClasses = ModuleInitializer.getJvmClasses(rootJarPath);
            jvmClasses.forEach(jarModule::addClass);

            javaModule.addImplementationDependency(jarModule);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void addOrUpdateFile(JavaModuleImpl module, Path path) {
        SourceClassInfoBuilder sourceClassInfoBuilder = new SourceClassInfoBuilder(path);
        module.addClass(sourceClassInfoBuilder.build());
    }

}
