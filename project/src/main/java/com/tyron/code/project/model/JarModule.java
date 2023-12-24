package com.tyron.code.project.model;

import com.google.common.collect.ImmutableSet;

import java.nio.file.Path;
import java.util.*;

public class JarModule extends ModuleWithSourceFiles {

    public static JarModule createJarDependency(Path jarPath) {
        return new JarModule(ModuleType.JAR_DEPENDENCY, jarPath);
    }

    public static JarModule createJdkDependency(Path jarPath) {
        return new JarModule(ModuleType.JDK, jarPath);
    }

    private final Path jarPath;

    private final Map<Path, UnparsedJavaFile> classMap;


    private JarModule(ModuleType moduleType, Path jarPath) {
        super(moduleType, jarPath.getFileName().toString());
        this.jarPath = jarPath;

        classMap = new HashMap<>();
    }

    public Path getJarPath() {
        return jarPath;
    }

    @Override
    public void addClass(UnparsedJavaFile unparsedJavaFile) {
        super.addClass(unparsedJavaFile);

        classMap.put(unparsedJavaFile.path(), unparsedJavaFile);
    }

    @Override
    public void removeFileFromPackage(UnparsedJavaFile file) {
        super.removeFileFromPackage(file);
        classMap.remove(file.path());
    }

    public Set<UnparsedJavaFile> getClasses() {
        return ImmutableSet.copyOf(classMap.values());
    }
}
