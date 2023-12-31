package com.tyron.code.info;

import com.tyron.code.info.builder.AbstractClassInfoBuilder;
import com.tyron.code.info.builder.SourceClassInfoBuilder;

import java.nio.file.Path;

public class SourceClassInfoImpl extends BasicClassInfo implements SourceClassInfo{

    private Path path;

    public SourceClassInfoImpl(SourceClassInfoBuilder builder) {
        super(builder);

        path = builder.getPath();
    }

    @Override
    public Path getPath() {
        return path;
    }
}
