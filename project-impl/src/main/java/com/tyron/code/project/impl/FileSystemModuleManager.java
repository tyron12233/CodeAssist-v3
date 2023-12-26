package com.tyron.code.project.impl;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
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

    private final JavaModuleImpl projectModule;

    private final FileManager fileManager;

    public FileSystemModuleManager(FileManager fileManager, Path root) {
        this.fileManager = fileManager;
        this.projectModule = new JavaModuleImpl(root);
        this.root = root;
    }

    @Override
    public Module getRootModule() {
        return projectModule;
    }

    @Override
    public void initialize() {
        walkDirectory(root);
    }

    @Override
    public synchronized Optional<JavaFileInfo> getFileItem(Path path) {
        return getFileItem(path, false);
    }

    private Optional<JavaFileInfo> getFileItem(Path path, boolean visitDeps) {
        Deque<JavaModule> queue = new LinkedList<>();
        Set<JavaModule> visited = new HashSet<>();
        queue.addLast(projectModule);
        while (!queue.isEmpty()) {
            JavaModule module = queue.removeFirst();
            Optional<JavaFileInfo> file = module.getFile(path.toString());
            if (file.isPresent()) {
                return file;
            }

            visited.add(module);

            if (!visitDeps) {
                continue;
            }
            Set<Module> dependingModules = module.getCompileOnlyDependencies();
            for (Module dependingModule : dependingModules) {
                if (!(dependingModule instanceof JavaModule javaModule)) {
                    continue;
                }
                if (visited.contains(javaModule)) {
                    continue;
                }
                queue.add(javaModule);
            }
        }

        return Optional.empty();
    }

    @Override
    public void addOrUpdateFile(Path path) {
        Optional<JavaFileInfo> existing = getFileItem(path, true);
        Module module = existing.isPresent() ? existing.get().module() : projectModule;
        if (module instanceof JavaModule javaModule) {
            addOrUpdateFile(javaModule, path);
        }
    }

    @Override
    public void removeFile(Path path) {

    }

    @Override
    public void addDependingModule(Module module) {
        projectModule.addImplementationDependency(module);
    }

    private void walkDirectory(Path root) {
        ImmutableMap<String, Consumer<Path>> handlers =
                ImmutableMap.of(
                        JAVA_EXTENSION,
                        path -> addOrUpdateFile(projectModule, path),
                        JAR_EXTENSION,
                        this::addJarModule
                );

        PathUtils.walkDirectory(root, handlers, fileManager::shouldIgnorePath);
    }

    private void addJarModule(Path path) {
        try {
            JarModuleImpl jarModule = new JarModuleImpl(path);
            Path rootJarPath = PathUtils.getRootPathForJarFile(path);

            List<JarReader.ClassInfo> infos = JarReader.readJarFile(path);
            infos.stream().map(it -> new JavaFileInfo(jarModule, rootJarPath, it.className(), it.packageQualifiers())).forEach(jarModule::addClass);

            projectModule.addImplementationDependency(jarModule);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void addOrUpdateFile(JavaModule module, Path path) {
        String name = path.getFileName().toString();
        if (name.endsWith(".java")) {
            name = name.substring(0, name.length() - ".java".length());
        }
        List<String> qualifiers = Arrays.stream(StringSearch.packageName(path).split("\\.")).toList();

        if (qualifiers.size() == 1 && qualifiers.get(0).isEmpty()) {
            qualifiers = Collections.emptyList();
        }

        JavaFileInfo javaFileInfo = new JavaFileInfo(module, path, name, qualifiers);
        ((JavaModuleImpl) module).addClass(javaFileInfo);
    }

}
