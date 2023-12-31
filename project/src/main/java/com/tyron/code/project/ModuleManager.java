package com.tyron.code.project;

import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.RootModule;

import java.nio.file.Path;
import java.util.Optional;

public interface ModuleManager {

    /**
     * Initialize the module manager. The module manager can start index files and building modules.
     * The module manager may also choose to defer indexing files to optimize for large repository.
     */
    void initialize();

    Optional<JavaFileInfo> getFileItem(Path path);

    void addOrUpdateFile(Path path);

    /** Remove a file from modules. */
    void removeFile(Path path);

    /** Add a module that all modules loaded by the module manager depends on. */
    void addDependingModule(Module module);

    RootModule getRootModule();
}
