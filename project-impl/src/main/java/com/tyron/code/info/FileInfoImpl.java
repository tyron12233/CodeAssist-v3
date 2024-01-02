package com.tyron.code.info;

import com.tyron.code.info.builder.FileInfoBuilder;
import com.tyron.code.info.properties.Property;
import com.tyron.code.info.properties.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

public class FileInfoImpl implements FileInfo {

    private final PropertyContainer properties;
    private final String name;
    private final byte[] rawContent;

    /**
     * @param name
     * 		File name/path.
     * @param rawContent
     * 		Raw contents of file.
     * @param properties
     * 		Assorted properties.
     */
    public FileInfoImpl(String name, byte[] rawContent, PropertyContainer properties) {
        this.name = name;
        this.rawContent = rawContent;
        this.properties = properties;
    }

    public FileInfoImpl(FileInfoBuilder<?> builder) {
        this.name = builder.getName();
        this.properties = builder.getProperties();
        this.rawContent = builder.getRawContent();
    }

    @Override
    public byte @NotNull [] getRawContent() {
        return rawContent;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfoImpl other = (FileInfoImpl) o;

        if (!name.equals(other.name)) return false;
        return Arrays.equals(rawContent, other.rawContent);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(rawContent);
        return result;
    }

    @Override
    public <V> void setProperty(Property<V> property) {
        properties.setProperty(property);
    }

    @Override
    public void removeProperty(String key) {
        properties.removeProperty(key);
    }

    @Nonnull
    @Override
    public Map<String, Property<?>> getProperties() {
        return properties.getProperties();
    }

    @Override
    public String toString() {
        return name;
    }
}
