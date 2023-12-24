package com.tyron.code.project;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.Module;
import com.tyron.code.project.model.*;
import com.tyron.code.project.util.JarReader;
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

    private final ProjectModule projectModule;

    private final FileManager fileManager;

    public FileSystemModuleManager(FileManager fileManager, Path root) {
        this.fileManager = fileManager;
        this.projectModule = new ProjectModule("");
        projectModule.setDirectory(root);
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


    public ProjectModule getProjectModule() {
        return projectModule;
    }

    @Override
    public synchronized Optional<UnparsedJavaFile> getFileItem(Path path) {
        return getFileItem(path, false);
    }

    private Optional<UnparsedJavaFile> getFileItem(Path path, boolean visitDeps) {
        Deque<ProjectModule> queue = new LinkedList<>();
        Set<ProjectModule> visited = new HashSet<>();
        queue.addLast(projectModule);
        while (!queue.isEmpty()) {
            ProjectModule module = queue.removeFirst();
            Optional<UnparsedJavaFile> file = module.getFile(path.toString());
            if (file.isPresent()) {
                return file;
            }

            visited.add(module);

            if (!visitDeps) {
                continue;
            }
            List<Module> dependingModules = module.getDependingModules(DependencyType.COMPILE_TIME);
            for (Module dependingModule : dependingModules) {
                if (dependingModule.getModuleType() != ModuleType.PROJECT) {
                    continue;
                }
                ProjectModule dependingProjectModule = (ProjectModule) dependingModule;
                if (visited.contains(dependingProjectModule)) {
                    continue;
                }
                queue.add(dependingProjectModule);
            }
        }

        return Optional.empty();
    }

    @Override
    public void addOrUpdateFile(Path path) {
        Optional<UnparsedJavaFile> existing = getFileItem(path, true);
        Module module = existing.isPresent() ? existing.get().module() : projectModule;
        if (module instanceof ProjectModule project) {
            addOrUpdateFile(project, path);
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
            JarModule jarModule = JarModule.createJarDependency(path);
            Path rootJarPath = PathUtils.getRootPathForJarFile(path);

            List<JarReader.ClassInfo> infos = JarReader.readJarFile(path);
            infos.stream().map(it -> new UnparsedJavaFile(jarModule, rootJarPath, it.className(), it.packageQualifiers())).forEach(jarModule::addClass);

            projectModule.addImplementationDependency(jarModule);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void addOrUpdateFile(ProjectModule module, Path path) {
        String name = path.getFileName().toString();
        if (name.endsWith(".java")) {
            name = name.substring(0, name.length() - ".java".length());
        }
        List<String> qualifiers = Arrays.stream(StringSearch.packageName(path).split("\\.")).toList();

        if (qualifiers.size() == 1 && qualifiers.get(0).isEmpty()) {
            qualifiers = Collections.emptyList();
        }

        UnparsedJavaFile unparsedJavaFile = new UnparsedJavaFile(module, path, name, qualifiers);
        module.addOrReplaceFile(unparsedJavaFile);
    }

}
