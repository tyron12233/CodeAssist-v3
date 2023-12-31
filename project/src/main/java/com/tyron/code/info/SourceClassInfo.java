package com.tyron.code.info;

import java.nio.file.Path;

/**
 * Outline of a source file
 */
public interface SourceClassInfo extends ClassInfo {

    /**
     * @return Path to the source file.
     */
    Path getPath();
}
