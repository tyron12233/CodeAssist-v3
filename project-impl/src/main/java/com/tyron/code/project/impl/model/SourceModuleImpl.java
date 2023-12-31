package com.tyron.code.project.impl.model;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.PackageScope;
import com.tyron.code.project.model.module.SourceModule;

import java.nio.file.Path;
import java.util.*;

public class SourceModuleImpl<T extends ClassInfo> extends AbstractModule implements SourceModule<T> {
    private final List<T> classInfos;

    public SourceModuleImpl(Path root) {
        super(root);
        this.classInfos = new ArrayList<>();
    }

    public void addClass(T info) {
        classInfos.add(info);
    }

    @Override
    public List<T> getFiles() {
        return classInfos;
    }
}
