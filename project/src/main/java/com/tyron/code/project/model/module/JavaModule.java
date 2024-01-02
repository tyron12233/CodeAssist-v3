package com.tyron.code.project.model.module;

import com.tyron.code.info.SourceClassInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface JavaModule extends SourceModule<SourceClassInfo> {

    Set<Module> getRuntimeOnlyDependencies();

    Set<Module> getCompileOnlyDependencies();

    Path getSourceDirectory();

    /**
     *
     * @return the JDK jar this project depends on
     */
    JdkModule getJdkModule();

    Set<SourceClassInfo> getSourceFiles();
}
