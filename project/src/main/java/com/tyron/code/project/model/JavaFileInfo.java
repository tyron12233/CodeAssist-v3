package com.tyron.code.project.model;

import com.tyron.code.project.model.module.Module;

import  java.nio.file.Path;
import java.util.List;

public record JavaFileInfo(
        Module module,
        Path path,
        String fileName,
        List<String> qualifiers
) {

}
