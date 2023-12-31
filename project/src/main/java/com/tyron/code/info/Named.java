package com.tyron.code.info;

import org.jetbrains.annotations.NotNull;

/**
 * Outline of a type that can be identified by name.
 */
public interface Named {
	/**
	 * @return Identifying name.
	 */
	@NotNull
	String getName();
}