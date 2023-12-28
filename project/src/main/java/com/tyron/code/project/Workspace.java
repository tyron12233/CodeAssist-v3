package com.tyron.code.project;

import java.nio.file.Path;

public interface Workspace {
    void close();

    Path getRoot();
}
