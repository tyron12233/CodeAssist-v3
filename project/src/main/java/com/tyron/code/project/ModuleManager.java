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

    default void addOrUpdateFile(Path path) {
        throw new UnsupportedOperationException();
    }

    /** Remove a file from modules. */
    default void removeFile(Path path) {
        throw new UnsupportedOperationException();
    }

    /** Add a module that all modules loaded by the module manager depends on. */
    default void addDependingModule(Module module) {
        throw new UnsupportedOperationException();
    }

    RootModule getRootModule();

    /**
     * Find the module that contains the given file.
     * @param file The file to find the module for.
     * @return The module that contains the given file, or empty if no module contains the file.
     */
    default Optional<Module> findModuleByFile(Path file) {
        return Optional.empty();
    }

    /**
     * Find a module by its name.
     * @param name The name of the module to find.
     * @return The module with the given name, or empty if no module with the given name exists.
     */
    default Optional<Module> findModuleByName(String name) {
        return Optional.empty();
    }
}
