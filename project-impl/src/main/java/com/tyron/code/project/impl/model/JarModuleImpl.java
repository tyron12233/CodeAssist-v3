package com.tyron.code.project.impl.model;

import com.tyron.code.info.JvmClassInfo;
import com.tyron.code.project.model.module.JarModule;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JarModuleImpl extends SourceModuleImpl<JvmClassInfo> implements JarModule {
    private final Path path;

    private final List<JvmClassInfo> classInfos;

    public JarModuleImpl(Path path) {
        super(path);
        this.path = path;
        this.classInfos = new ArrayList<>();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public List<JvmClassInfo> getClasses() {
        return classInfos;
    }

    public void addClass(JvmClassInfo classInfo) {
        classInfos.add(classInfo);
    }
}
