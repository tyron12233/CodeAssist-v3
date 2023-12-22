package com.tyron.code.project.model;

import  java.nio.file.Path;
import java.util.List;

public record UnparsedJavaFile(
        Module module,
        Path path,
        String fileName,
        List<String> qualifiers
) {

}
