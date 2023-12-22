package com.tyron.code.project.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class JarModule extends Module {

    public static JarModule createJarDependency(Path jarPath) {
        return new JarModule(ModuleType.JAR_DEPENDENCY, jarPath);
    }

    public static JarModule createJdkDependency(Path jarPath) {
        return new JarModule(ModuleType.JDK, jarPath);
    }

    private final Path jarPath;
    private final Set<UnparsedJavaFile> classes;

    private JarModule(ModuleType moduleType, Path jarPath) {
        super(moduleType, jarPath.getFileName().toString());
        this.jarPath = jarPath;
        this.classes = new HashSet<>();
    }

    public Path getJarPath() {
        return jarPath;
    }

    public void addClass(UnparsedJavaFile unparsedJavaFile) {
        classes.add(unparsedJavaFile);
    }

    public Set<UnparsedJavaFile> getClasses() {
        return classes;
    }
}
