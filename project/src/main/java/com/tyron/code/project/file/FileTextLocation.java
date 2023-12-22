package com.tyron.code.project.file;

import com.google.auto.value.AutoValue;
import com.tyron.code.project.model.TextRange;

import java.nio.file.Path;

/** Represents a range in a file. */
@AutoValue
public abstract class FileTextLocation {

    public abstract Path getFilePath();

    public abstract TextRange getRange();

    public static FileTextLocation create(Path filePath, TextRange range) {
        return new AutoValue_FileTextLocation(filePath, range);
    }
}