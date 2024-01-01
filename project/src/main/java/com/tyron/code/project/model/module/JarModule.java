package com.tyron.code.project.model.module;

import com.tyron.code.info.JvmClassInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface JarModule extends SourceModule<JvmClassInfo> {
    Path getPath();

    Set<JvmClassInfo> getClasses();
}