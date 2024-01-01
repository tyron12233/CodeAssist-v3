package com.tyron.code.project.model.module;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.PackageScope;
import com.tyron.code.project.util.ClassNameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SourceModule<T extends ClassInfo>  extends Module {

    Set<T> getFiles();
}
