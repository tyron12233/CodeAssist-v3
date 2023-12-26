package com.tyron.code.project.model.module;

import java.nio.file.Path;

public interface JarModule extends SourceModule {
    Path getPath();
}