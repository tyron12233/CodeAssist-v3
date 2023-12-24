package com.tyron.code.project.model;

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
    private final Set<UnparsedJavaFile> classes;

    private final PackageScope rootPackage;

    private JarModule(ModuleType moduleType, Path jarPath) {
        super(moduleType, jarPath.getFileName().toString());
        this.jarPath = jarPath;
        this.classes = new HashSet<>();
        rootPackage = new PackageScope("");
    }

    public Path getJarPath() {
        return jarPath;
    }


    public Set<UnparsedJavaFile> getClasses() {
        return classes;
    }
}
