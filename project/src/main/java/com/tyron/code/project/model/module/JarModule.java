package com.tyron.code.project.model.module;

import com.tyron.code.info.JvmClassInfo;

import java.nio.file.Path;
import java.util.List;

public interface JarModule extends SourceModule<JvmClassInfo> {
    Path getPath();

    List<JvmClassInfo> getClasses();
}