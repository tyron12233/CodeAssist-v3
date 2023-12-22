package com.tyron.code.project;

/**
 * Thrown when there's an exception during project initialization such as
 * missing dependencies, error on configs, etc.
 */
public class InitializationException extends RuntimeException {

    public InitializationException() {

    }

    public InitializationException(String reason) {
        super(reason);
    }
}
