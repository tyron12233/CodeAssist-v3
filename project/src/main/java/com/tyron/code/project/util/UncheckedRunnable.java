package com.tyron.code.project.util;

/**
 * Its {@link Runnable} but can throw an exception.
 *
 * @author Matt Coley
 */
@FunctionalInterface
public interface UncheckedRunnable extends Runnable {
	@Override
	default void run() {
		try {
			uncheckedRun();
		} catch (Throwable t) {
			ReflectUtil.propagate(t);
		}
	}

	/**
	 * @throws Throwable
	 * 		Whenever.
	 */
	void uncheckedRun() throws Throwable;
}