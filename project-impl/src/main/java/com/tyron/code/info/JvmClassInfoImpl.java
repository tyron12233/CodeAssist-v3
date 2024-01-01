package com.tyron.code.info;

import com.tyron.code.info.builder.JvmClassInfoBuilder;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;

public class JvmClassInfoImpl extends BasicClassInfo implements JvmClassInfo {

    private final int version;

    private final byte[] bytecode;
    private ClassReader reader;

    public JvmClassInfoImpl(JvmClassInfoBuilder builder) {
        super(builder);

        this.version = builder.getVersion();
        this.bytecode = builder.getBytecode();
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public byte @NotNull [] getBytecode() {
        return bytecode;
    }

    @Override
    public @NotNull ClassReader getClassReader() {
        if (reader == null) {
            reader = new ClassReader(bytecode);
        }
        return reader;
    }
}
