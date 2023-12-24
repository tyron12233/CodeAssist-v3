package com.tyron.code.project.util;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class JarReader {

    public static List<ClassInfo> readJarFile(String jarPath) throws IOException {
        List<ClassInfo> classInfos = new ArrayList<>();

        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + Paths.get(jarPath).toUri()), Map.of())) {
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".class")) {
                        String classPath = getFqn(file.toString());
                        String className = getClassName(classPath);

                        List<String> qualifiers = getAsQualifierList(classPath.substring(0, className.length() + ".class".length()));

                        ClassInfo classInfo = new ClassInfo();
                        classInfo.setPackageQualifiers(qualifiers);
                        classInfo.setClassName(className);
                        classInfos.add(classInfo);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return classInfos;
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

    public static class ClassInfo {
        private List<String> packageQualifiers;
        private String className;

        public List<String> getPackageQualifiers() {
            return packageQualifiers;
        }

        public void setPackageQualifiers(List<String> packageQualifiers) {
            this.packageQualifiers = packageQualifiers;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }
}
