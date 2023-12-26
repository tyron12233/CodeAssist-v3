package com.tyron.code.project.model.module;

import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.PackageScope;
import com.tyron.code.project.util.ClassNameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public interface SourceModule extends Module {

    default Optional<PackageScope> getPackage(String packageName) {
        return getPackage(ClassNameUtils.getAsQualifierList(packageName));
    }

    Optional<PackageScope> getPackage(List<String> qualifiers);


    default Optional<JavaFileInfo> getFile(String pathString) {
        return getFile(Paths.get(pathString));
    }

    Optional<JavaFileInfo> getFile(Path path);

    List<JavaFileInfo> getFiles();
}
