package com.tyron.code.project.util;

import com.tyron.code.project.model.UnparsedJavaFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class JarReader {

    public static List<ClassInfo> readJarFile(String jarPath) throws IOException {
        List<ClassInfo> classInfos = new ArrayList<>();

        // Create a zip file system to access the JAR file
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + Paths.get(jarPath).toUri()), Map.of())) {
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        String classPath = file.toString().replace("/", ".");
                        String className = classPath.substring(0, classPath.length() - ".class".length());


                        String packageQualifiers = getPackageQualifiers(className);
                        List<String> list = Arrays.stream(packageQualifiers.split("\\.")).toList();
                        if (packageQualifiers.isEmpty()) {
                            list = Collections.emptyList();
                        }

                        ClassInfo classInfo = new ClassInfo();
                        classInfo.setPackageQualifiers(list);
                        classInfo.setClassName(className.substring(packageQualifiers.length() + 1));
                        classInfos.add(classInfo);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return classInfos;
    }

    private static String getPackageQualifiers(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "";
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
