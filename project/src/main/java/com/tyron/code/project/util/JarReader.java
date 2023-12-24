package com.tyron.code.project.util;

import com.google.common.annotations.VisibleForTesting;
import com.tyron.code.project.model.JarModule;
import com.tyron.code.project.model.UnparsedJavaFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class JarReader {

    public static List<ClassInfo> readJarFile(Path path) throws IOException {
        List<ClassInfo> classInfos = new ArrayList<>();


        FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), Map.of());
        Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".class")) {
                    String classPath = getFqn(file.toString());
                    String className = getClassName(classPath);

                    List<String> qualifiers = getAsQualifierList(getPackageOnly(classPath));

                    ClassInfo classInfo = new ClassInfo(qualifiers, className, file);
                    classInfos.add(classInfo);
                }
                return FileVisitResult.CONTINUE;
            }
        });


        return classInfos;
    }

    public static JarModule toJarModule(Path path, boolean isJdk) throws IOException {
        List<ClassInfo> infos = readJarFile(path);
        JarModule module = isJdk ? JarModule.createJdkDependency(path) : JarModule.createJarDependency(path);
        infos.stream().map(it -> new UnparsedJavaFile(module, it.classPath(), it.className(), it.packageQualifiers()))
                .forEach(module::addClass);
        return module;
    }

    public static String getPackageOnly(String fqn) {
        if (fqn.endsWith(".class")) {
            fqn = fqn.substring(0, fqn.length() - ".class".length());
        }

        String className = getClassName(fqn);
        return fqn.substring(0, fqn.length() - className.length() - 1);
    }

    /**
     * Returns the Fully Qualified Name of a classpath
     */
    @VisibleForTesting
    public static String getFqn(String path) {
        String classPath = path.replace("/", ".");
        if (classPath.startsWith(".")) {
            classPath = classPath.substring(1);
        }
        return classPath.substring(0, classPath.length() - ".class".length());
    }

    @VisibleForTesting
    public static String getClassName(String fqn) {
        if (!fqn.contains(".")) {
            return fqn;
        }

        return fqn.substring(fqn.lastIndexOf('.') + 1);
    }

    public static List<String> getAsQualifierList(String className) {
        List<String> qualifiers = Arrays.stream(className.split("\\.")).toList();
        if (qualifiers.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> validQualifiers = new ArrayList<>(qualifiers.size());
        for (String qualifier : qualifiers) {
            if (qualifier.isEmpty()) {
                continue;
            }

            validQualifiers.add(qualifier);
        }
        return validQualifiers;
    }

    public record ClassInfo(List<String> packageQualifiers, String className, Path classPath) {

    }
}
