package com.tyron.code.info;

import com.tyron.code.info.properties.PropertyContainer;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Outline of all info types.
 */
public interface Info extends PropertyContainer, Serializable {

    @NotNull
    String getName();
}
