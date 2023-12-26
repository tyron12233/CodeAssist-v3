package com.tyron.code.project.impl;

import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JdkModuleImpl;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JdkModule;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static com.tyron.code.project.util.ClassNameUtils.*;

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

    public static JarModule toJarModule(Path path) throws IOException {
        List<ClassInfo> infos = readJarFile(path);
        JarModuleImpl jarModule = new JarModuleImpl(path);
        infos.stream().map(it -> new JavaFileInfo(jarModule, it.classPath(), it.className(), it.packageQualifiers()))
                .forEach(jarModule::addClass);
        return jarModule;
    }

    public static JdkModule toJdkModule(Path path) throws IOException {
        List<ClassInfo> infos = readJarFile(path);
        JdkModuleImpl jarModule = new JdkModuleImpl(path, "11");
        infos.stream().map(it -> new JavaFileInfo(jarModule, it.classPath(), it.className(), it.packageQualifiers()))
                .forEach(jarModule::addClass);
        return jarModule;
    }

    public record ClassInfo(List<String> packageQualifiers, String className, Path classPath) {

    }
}
