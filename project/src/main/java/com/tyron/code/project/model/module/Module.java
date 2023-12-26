package com.tyron.code.project.model.module;

import java.nio.file.Path;
import java.util.List;

public interface Module {
    String getName();

    List<Module> getDependencies();

    Path getRootDirectory();
}
