package com.tyron.code.project.model.module;

import com.tyron.code.info.ClassInfo;

import java.util.Set;

public interface SourceModule<T extends ClassInfo>  extends Module {

    Set<T> getSourceFiles();
}
