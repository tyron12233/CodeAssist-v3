package com.tyron.code.info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FileInfo extends Info, Named {

    /**
     * @return Raw bytes of file content.
     */

    byte @NotNull [] getRawContent();

    /**
     * @return Directory the file resides in.
     * May be {@code null} for files in the root directory.
     */
    @Nullable
    default String getDirectoryName() {
        String fileName = getName();
        int directoryIndex = fileName.lastIndexOf('/');
        if (directoryIndex <= 0) {
            return null;
        }
        return fileName.substring(0, directoryIndex);
    }
}
