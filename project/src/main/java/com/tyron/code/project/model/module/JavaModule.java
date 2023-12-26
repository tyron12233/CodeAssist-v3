package com.tyron.code.project.model.module;

import java.nio.file.Path;
import java.util.Set;

public interface JavaModule extends SourceModule {

    Set<Module> getRuntimeOnlyDependencies();

    Set<Module> getCompileOnlyDependencies();

    Path getSourceDirectory();

    /**
     *
     * @return the JDK jar this project depends on
     */
    JdkModule getJdkModule();
}
