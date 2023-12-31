package com.tyron.code.project.util;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Util to access {@link Unsafe}.
 *
 * @author xDark
 */
public class UnsafeUtil {
	private static final Unsafe UNSAFE;

	/**
	 * @return The unsafe instance.
	 */
	@NotNull
	public static Unsafe get() {
		return UNSAFE;
	}

	static {
		try {
			Unsafe unsafe = null;
			for (Field field : Unsafe.class.getDeclaredFields()) {
				if (Unsafe.class == field.getType()) {
					field.setAccessible(true);
					unsafe = (Unsafe) field.get(null);
					break;
				}
			}
			if (unsafe == null)
				throw new IllegalStateException("Unable to locate unsafe instance");
			UNSAFE = unsafe;
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}