package com.tyron.code.info.builder;

import com.tyron.code.info.FileInfo;
import com.tyron.code.info.FileInfoImpl;
import com.tyron.code.info.properties.BasicPropertyContainer;
import com.tyron.code.info.properties.Property;
import com.tyron.code.info.properties.PropertyContainer;
import com.tyron.code.project.util.StringUtil;
import com.tyron.code.project.util.Unchecked;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfoBuilder<B extends FileInfoBuilder<?>> {

    private PropertyContainer properties = new BasicPropertyContainer();
    private String name;
    private byte[] rawContent;

    public FileInfoBuilder() {
        // default
    }

    public FileInfoBuilder(Path file) {
        withName(file.toString());
        withRawContent(Unchecked.get(() -> Files.readAllBytes(file)));
        withProperties(new BasicPropertyContainer());
    }

    protected FileInfoBuilder(@NotNull FileInfo fileInfo) {
        // copy state
        withName(fileInfo.getName());
        withRawContent(fileInfo.getRawContent());
        withProperties(new BasicPropertyContainer(fileInfo.getProperties()));
    }

    protected FileInfoBuilder(@NotNull FileInfoBuilder<?> other) {
        withName(other.getName());
        withRawContent(other.getRawContent());
        withProperties(other.getProperties());
    }

    @SuppressWarnings("unchecked")
    public B withProperties(@NotNull PropertyContainer properties) {
        this.properties = properties;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withProperty(@NotNull Property<?> property) {
        properties.setProperty(property);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withName(@NotNull String name) {
        this.name = name;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withRawContent(@NotNull byte[] rawContent) {
        this.rawContent = rawContent;
        return (B) this;
    }

    public PropertyContainer getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public byte[] getRawContent() {
        return rawContent;
    }

    @NotNull
    public FileInfoImpl build() {
        if (name == null) throw new IllegalArgumentException("Name is required");
        if (rawContent == null) throw new IllegalArgumentException("Content is required");
//        if (StringUtil.isText(rawContent)) {
//            return new BasicTextFileInfo(this);
//        } else
        return new FileInfoImpl(this);
    }
}