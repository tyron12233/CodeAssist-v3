package com.tyron.code.project.model;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.properties.Property;
import com.tyron.code.project.model.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import  java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record JavaFileInfo(
        Module module,
        Path path,
        String fileName,
        List<String> qualifiers
) implements ClassInfo {

    @Override
    public @Nullable String getSourceFileName() {
        return fileName;
    }

    @Override
    public @NotNull List<String> getInterfaces() {
        return null;
    }

    @Override
    public @Nullable String getSuperName() {
        return null;
    }

    @Override
    public String getSignature() {
        return null;
    }

    @Override
    public @Nullable String getOuterClassName() {
        return null;
    }

    @Override
    public @Nullable String getOuterMethodName() {
        return null;
    }

    @Override
    public @Nullable String getOuterMethodDescriptor() {
        return null;
    }

    @Override
    public @NotNull String getName() {
        return null;
    }

    @Override
    public <V> void setProperty(Property<V> property) {

    }

    @Override
    public void removeProperty(String key) {

    }

    @Override
    public @NotNull Map<String, Property<?>> getProperties() {
        return null;
    }
}
