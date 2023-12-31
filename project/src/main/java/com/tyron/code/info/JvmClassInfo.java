package com.tyron.code.info;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;

/**
 * Outline of a JVM Class
 */
public interface JvmClassInfo extends ClassInfo {

    /**
     * Denotes the base version offset.
     * <ul>
     *     <li>For version 1 of you would use {@code BASE_VERSION + 1}.</li>
     *     <li>For version 2 of you would use {@code BASE_VERSION + 2}.</li>
     *     <li>...</li>
     *     <li>For version N of you would use {@code BASE_VERSION + N}.</li>
     * </ul>
     */
    int BASE_VERSION = 44;

    /**
     * @return Java class file version.
     */
    int getVersion();

    /**
     * @return Bytecode of class.
     */
    byte @NotNull [] getBytecode();

    /**
     * @return Class reader of {@link #getBytecode()}.
     */
    @NotNull
    ClassReader getClassReader();
}
