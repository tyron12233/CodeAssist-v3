package com.tyron.code.project.model.module;

import com.tyron.code.info.FileInfo;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public interface Module extends Serializable {

    String CONFIG_NAME = "project.toml";

    default Path getModuleConfig() {
        return getRootDirectory().resolve(CONFIG_NAME);
    }

    String getName();

    List<Module> getDependencies();

    Path getRootDirectory();

    default List<FileInfo> getFiles() {
        return List.of();
    }
}
