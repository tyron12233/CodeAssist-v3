package com.tyron.code.project.impl.model;

import com.tyron.code.project.model.module.JarModule;

import java.nio.file.Path;

public class JarModuleImpl extends SourceModuleImpl implements JarModule {
    private final Path path;

    public JarModuleImpl(Path path) {
        super(path);
        this.path = path;
    }

    @Override
    public Path getPath() {
        return path;
    }
}
