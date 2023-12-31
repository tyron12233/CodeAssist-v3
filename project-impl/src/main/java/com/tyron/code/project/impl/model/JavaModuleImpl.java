package com.tyron.code.project.impl.model;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.JdkModule;
import com.tyron.code.project.model.module.Module;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class JavaModuleImpl extends SourceModuleImpl<SourceClassInfo> implements JavaModule {

    private final Set<Module> compileOnlyDependencies;
    private final Set<Module> runtimeOnlyDependencies;
    private final Path rootDirectory;
    private JdkModule jdkModule;

    public JavaModuleImpl(Path rootDirectory) {
        super(rootDirectory);
        this.rootDirectory = rootDirectory;
        this.compileOnlyDependencies = new HashSet<>();
        this.runtimeOnlyDependencies = new HashSet<>();
    }

    @Override
    public void addClass(SourceClassInfo info) {
        super.addClass(info);
    }

    @Override
    public List<SourceClassInfo> getFiles() {
        return super.getFiles();
    }

    @Override
    public Set<Module> getRuntimeOnlyDependencies() {
        return Collections.unmodifiableSet(runtimeOnlyDependencies);
    }

    @Override
    public Set<Module> getCompileOnlyDependencies() {
        return Collections.unmodifiableSet(compileOnlyDependencies);
    }

    @Override
    public Path getSourceDirectory() {
        return rootDirectory.resolve("src/main/java");
    }

    public void addImplementationDependency(Module module) {
        runtimeOnlyDependencies.add(module);
        compileOnlyDependencies.add(module);
    }

    public void setJdk(JdkModule jdkModule) {
        this.jdkModule = jdkModule;
    }

    @Override
    public JdkModule getJdkModule() {
        return jdkModule;
    }

    @Override
    public List<Module> getDependencies() {
        return Stream.concat(
                compileOnlyDependencies.stream(),
                runtimeOnlyDependencies.stream()
        ).toList();
    }

    public void addRuntimeOnly(Module module) {
        runtimeOnlyDependencies.add(module);
    }

    public void addCompileOnly(Module module) {
        compileOnlyDependencies.add(module);
    }
}
