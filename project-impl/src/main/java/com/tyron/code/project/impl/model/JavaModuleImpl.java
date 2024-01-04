package com.tyron.code.project.impl.model;

import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.JdkModule;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaModuleImpl extends SourceModuleImpl<SourceClassInfo> implements JavaModule {

    private final Set<String> compileOnlyDependencies;
    private final Set<String> runtimeOnlyDependencies;
    private final Path rootDirectory;
    private JdkModule jdkModule;

    public JavaModuleImpl(ModuleManager moduleManager, Path rootDirectory) {
        super(moduleManager, rootDirectory);
        this.rootDirectory = rootDirectory;
        this.compileOnlyDependencies = new HashSet<>();
        this.runtimeOnlyDependencies = new HashSet<>();
    }

    @Override
    public void addClass(SourceClassInfo info) {
        super.addClass(info);
    }

    @Override
    public Set<SourceClassInfo> getSourceFiles() {
        return super.getSourceFiles();
    }

    @Override
    public Set<Module> getRuntimeOnlyDependencies() {
        ModuleManager moduleManager = getModuleManager();
        return runtimeOnlyDependencies.stream()
                .map(moduleManager::findModuleByName)
                .map(Optional::orElseThrow)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Module> getCompileOnlyDependencies() {
        ModuleManager moduleManager = getModuleManager();
        return compileOnlyDependencies.stream()
                .map(moduleManager::findModuleByName)
                .map(Optional::orElseThrow)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Path getSourceDirectory() {
        return rootDirectory.resolve("src/main/java");
    }

    public void addImplementationDependency(Module module) {
        runtimeOnlyDependencies.add(module.getName());
        compileOnlyDependencies.add(module.getName());
    }

    public void setJdk(JdkModule jdkModule) {
        this.jdkModule = jdkModule;
    }

    @Override
    public JdkModule getJdkModule() {
        return jdkModule;
    }

    @Override
    public @NotNull List<String> getDependencies() {
        return Stream.concat(
                compileOnlyDependencies.stream(),
                runtimeOnlyDependencies.stream()
        ).toList();
    }

    public void addRuntimeOnly(Module module) {
        runtimeOnlyDependencies.add(module.getName());
    }

    public void addCompileOnly(Module module) {
        compileOnlyDependencies.add(module.getName());
    }
}
