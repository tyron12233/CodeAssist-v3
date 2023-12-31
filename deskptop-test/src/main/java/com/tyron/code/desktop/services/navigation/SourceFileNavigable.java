package com.tyron.code.desktop.services.navigation;

import com.tyron.code.path.impl.SourceClassPathNode;
import org.jetbrains.annotations.NotNull;

public interface SourceFileNavigable extends Navigable {

    @NotNull
    @Override
    SourceClassPathNode getPath();
}
