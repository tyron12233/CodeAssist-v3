package com.tyron.code.project;

import com.google.common.collect.ImmutableList;
import com.moandjiezana.toml.Toml;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.graph.CompileProjectModuleBFS;
import com.tyron.code.project.model.*;
import com.tyron.code.project.model.Module;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Parses CodeAssist's projects, handling project dependency and others
 */
public class CodeAssistModuleManager implements ModuleManager {

    private static final String PROJECT_CONFIG_NAME = "project.toml";


    private final FileManager fileManager;
    private final Path rootDirectory;

    private final Map<String, ProjectModule> includedProjects;

    private ProjectModule rootProject;

    public CodeAssistModuleManager(FileManager fileManager, Path rootDirectory) {
        this.fileManager = fileManager;
        this.rootDirectory = rootDirectory;
        includedProjects = new HashMap<>();
    }

    @Override
    public void initialize() {
        Path configFile = rootDirectory.resolve(PROJECT_CONFIG_NAME);
        Config config = new Toml().read(configFile.toFile()).to(Config.class);

        ProjectModule rootProject = new ProjectModule(config.settings.projectName);
        List<Path> includedProjectPaths = config.settings.include.stream().map(rootDirectory::resolve).toList();

        Map<ProjectModule, Config> configMap = new HashMap<>();
        includedProjectPaths.forEach(includedProjectPath -> processIncludedProject(includedProjectPath, configMap));

        resolveProjectDependencies(configMap);

        this.rootProject = rootProject;
    }

    private void processIncludedProject(Path includedProjectPath, Map<ProjectModule, Config> configMap) {
        CharSequence configContents = fileManager.getFileContent(includedProjectPath.resolve(PROJECT_CONFIG_NAME)).orElseThrow(() -> new InitializationException("Unable to read config file in " + includedProjectPath));
        Config includedProjectConfig = new Toml().read(configContents.toString()).to(Config.class);
        String projectName = includedProjectConfig.settings.projectName;
        ProjectModule projectModule = new ProjectModule(projectName);
        includedProjects.put(projectName, projectModule);
        configMap.put(projectModule, includedProjectConfig);
    }


    private void resolveProjectDependencies(Map<ProjectModule, Config> configMap) {
        for (ProjectModule included : includedProjects.values()) {
            Config includedConfig = configMap.get(included);
            if (includedConfig == null) {
                throw new InitializationException("No config found for project " + included.getDebugName());
            }

            List<Dependency> dependencies = includedConfig.build.dependencies;
            if (dependencies == null) {
                dependencies = List.of();
            }

            for (Dependency dependency : dependencies) {
                if ("project".equals(dependency.type)) {
                    ProjectModule dependencyModule = includedProjects.get(dependency.notation);
                    if (dependencyModule != null) {
                        included.addImplementationDependency(dependencyModule);
                    } else {
                        String message = "Project " + included.getDebugName() + " depends on project "
                                + dependency.notation + " but it is not found or included in the root project.";
                        throw new InitializationException(message);
                    }
                } else {
                    // TODO: Handle other dependency types
                }
            }
        }
    }

    @Override
    public Optional<UnparsedJavaFile> getFileItem(Path path) {
        CompileProjectModuleBFS compileModuleBFS = new CompileProjectModuleBFS(rootProject);

        CompletableFuture<Optional<UnparsedJavaFile>> future = new CompletableFuture<>();
        compileModuleBFS.traverse(currentNode -> {
            if (Objects.requireNonNull(currentNode.getModuleType()) == ModuleType.PROJECT) {
                ProjectModule projectModule = ((ProjectModule) currentNode);

                Optional<UnparsedJavaFile> optional = projectModule.getFile(path.toAbsolutePath().toString());
                if (optional.isPresent()) {
                    future.complete(optional);
                }
            }
        });

        return future.join();
    }

    @Override
    public void addOrUpdateFile(Path path) {

    }

    @Override
    public void removeFile(Path path) {

    }

    @Override
    public void addDependingModule(Module module) {

    }

    public List<ProjectModule> getIncludedProjects() {
        return ImmutableList.copyOf(includedProjects.values());
    }

    @Override
    public Module getRootModule() {
        return rootProject;
    }
}
