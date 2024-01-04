package com.tyron.code.project.model.module;

import com.tyron.code.info.FileInfo;
import com.tyron.code.project.ModuleManager;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents a module in a project
 */
public interface Module extends Serializable {

    String CONFIG_NAME = "project.toml";

    /**
     * @return the path to the module configuration file
     */
    default Path getModuleConfig() {
        return getRootDirectory().resolve(CONFIG_NAME);
    }

    /**
     * @return the module manager object that manages this module
     */
    @NotNull
    ModuleManager getModuleManager();

    /**
     * @return the unique name of this module
     */
    @NotNull
    String getName();

    /**
     * @return a list of module names that this module depends on
     */
    @NotNull
    List<String> getDependencies();

    /**
     * @return the root directory of this module
     */
    @NotNull
    Path getRootDirectory();

    /**
     * @return a list of files that belong to this module, this may not
     * return all files in the module directory, as some files are considered
     * as part of the module, but not part of the source code
     * see {@link SourceModule#getSourceFiles()}
     */
    @NotNull
    default List<FileInfo> getFiles() {
        return List.of();
    }
}
