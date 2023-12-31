package com.tyron.code.info;

import com.tyron.code.info.properties.PropertyContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Outline of all info types.
 */
public interface Info extends PropertyContainer {

    @NotNull
    String getName();
}
