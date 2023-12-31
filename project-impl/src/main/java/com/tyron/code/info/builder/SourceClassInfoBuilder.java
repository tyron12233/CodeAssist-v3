package com.tyron.code.info.builder;

import com.google.common.io.Files;
import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.info.SourceClassInfoImpl;
import com.tyron.code.project.util.ClassNameUtils;
import com.tyron.code.project.util.StringSearch;

import java.nio.file.Path;

public class SourceClassInfoBuilder extends AbstractClassInfoBuilder<SourceClassInfoBuilder>{

    private Path path;

    public SourceClassInfoBuilder() {
        super();
    }

    public SourceClassInfoBuilder(SourceClassInfo info) {
        withPath(info.getPath());
    }

    public SourceClassInfoBuilder(Path path) {
        withPath(path);
        withSourceFileName(Files.getNameWithoutExtension(path.getFileName().toString()));
        withName(StringSearch.packageName(path).replace('.', '/') + "/" + getSourceFileName());
    }

    public SourceClassInfoBuilder withPath(Path path) {
        this.path = path;
        return this;
    }

    @Override
    public SourceClassInfo build() {
        return new SourceClassInfoImpl(this);
    }

    public Path getPath() {
        return path;
    }
}
