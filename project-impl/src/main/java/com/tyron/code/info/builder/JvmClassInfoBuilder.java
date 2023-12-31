package com.tyron.code.info.builder;

import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.JvmClassInfo;
import com.tyron.code.info.JvmClassInfoImpl;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class JvmClassInfoBuilder extends AbstractClassInfoBuilder<JvmClassInfoBuilder> {

    private byte[] bytecode;
    private int version = JvmClassInfo.BASE_VERSION + 8; // Java 8

    /**
     * Create empty builder.
     */
    public JvmClassInfoBuilder() {
        super();
    }

    /**
     * Create a builder with data pulled from the given class.
     *
     * @param classInfo
     * 		Class to pull data from.
     */
    public JvmClassInfoBuilder(@NotNull JvmClassInfo classInfo) {
        super(classInfo);
        withBytecode(classInfo.getBytecode());
        withVersion(classInfo.getVersion());
    }

    /**
     * Creates a builder with data pulled from the given bytecode.
     *
     * @param reader
     * 		ASM class reader to read bytecode from.
     */
    public JvmClassInfoBuilder(@NotNull ClassReader reader) {
        adaptFrom(reader);
    }

    /**
     * Creates a builder with the given bytecode.
     *
     * @param bytecode
     * 		Class bytecode to read values from.
     */
    public JvmClassInfoBuilder(byte @NotNull [] bytecode) {
        adaptFrom(new ClassReader(bytecode));
    }

    /**
     * Copies over values by reading the contents of the class file in the reader.
     * Calls {@link #adaptFrom(ClassReader, int)} with {@code flags=0}.
     *
     * @param reader
     * 		ASM class reader to pull data from.
     *
     * @return Builder.
     */
    @NotNull
    public JvmClassInfoBuilder adaptFrom(@NotNull ClassReader reader) {
        return adaptFrom(reader, 0);
    }

    /**
     * Copies over values by reading the contents of the class file in the reader.
     *
     * @param reader
     * 		ASM class reader to pull data from.
     * @param flags
     * 		Reader flags to use when populating information.
     *
     * @return Builder.
     */
    @NotNull
    @SuppressWarnings(value = "deprecation")
    public JvmClassInfoBuilder adaptFrom(@NotNull ClassReader reader, int flags) {
        reader.accept(new ClassBuilderAppender(), flags);
        return withBytecode(reader.b);
    }

    @NotNull
    public JvmClassInfoBuilder withBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
        return this;
    }

    @NotNull
    public JvmClassInfoBuilder withVersion(int version) {
        this.version = version;
        return this;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public int getVersion() {
        return version;
    }


    @Override
    public JvmClassInfo build() {
        verify();
        return new JvmClassInfoImpl(this);
    }

    @Override
    protected void verify() {
        super.verify();
        if (bytecode == null) {
            throw new IllegalStateException("Bytecode required");
        }
        if (version < JvmClassInfo.BASE_VERSION) {
            throw new IllegalStateException("Version cannot be lower than 44 (v1)");
        }
    }

    private class ClassBuilderAppender extends ClassVisitor {
//        private final List<AnnotationInfo> annotations = new ArrayList<>();
//        private final List<TypeAnnotationInfo> typeAnnotations = new ArrayList<>();
//        private final List<InnerClassInfo> innerClasses = new ArrayList<>();
//        private final List<FieldMember> fields = new ArrayList<>();
//        private final List<MethodMember> methods = new ArrayList<>();

        protected ClassBuilderAppender() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            withVersion(version & 0xFF);
//            withAccess(access);
            withName(name);
            withSignature(signature);
            withSuperName(superName);
            withInterfaces(Arrays.asList(interfaces));
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
            withSourceFileName(source);
        }

        @Override
        public void visitOuterClass(String owner, String name, String descriptor) {
            super.visitOuterClass(owner, name, descriptor);
            withOuterClassName(owner);
            withOuterMethodName(name);
            withOuterMethodDescriptor(descriptor);
        }

    }
}
