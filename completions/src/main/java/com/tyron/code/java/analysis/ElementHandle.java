package com.tyron.code.java.analysis;

import com.tyron.code.java.util.ClassFileUtil;
import com.tyron.code.java.util.ElementUtils;
import com.tyron.code.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.code.Symtab;
import shadow.com.sun.tools.javac.jvm.Target;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.javax.lang.model.element.*;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ElementHandle<T extends Element> {

    private static final Logger logger = Logging.get(ElementHandle.class);

    private final ElementKind kind;
    private final String[] signatures;

    private ElementHandle(final ElementKind kind, String... signatures) {
        assert kind != null;
        assert signatures != null;
        this.kind = kind;
        this.signatures = signatures;
    }

    /**
     * Resolves an {@link Element} from the {@link ElementHandle}.
     *
     * @param analysisResult representing the {@link shadow.javax.tools.JavaCompiler.CompilationTask}
     *                       in which the {@link Element} should be resolved.
     * @return resolved subclass of {@link Element} or null if the elment does not exist on
     * the classpath/sourcepath of {@link javax.tools.JavaCompiler.CompilationTask}.
     */
    public @CheckForNull T resolve(@NotNull final AnalysisResult analysisResult) {
        ModuleElement module = null;

        JCTree.JCCompilationUnit cut = (JCTree.JCCompilationUnit) analysisResult.parsedTree();
        if (cut != null) {
            module = cut.modle;
        }

        T result = resolveImpl(module, analysisResult.javacTask());
        if (result == null) {
            logger.info("Cannot resolve: {}", this);

        } else {
            logger.info("Resolved element = {}", result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private T resolveImpl(ModuleElement module, JavacTaskImpl jt) {
        ElementKind simplifiedKind = this.kind;
        if (simplifiedKind.name().equals("RECORD")) {
            simplifiedKind = ElementKind.CLASS; //TODO: test
        }
        if (simplifiedKind.name().equals("RECORD_COMPONENT")) {
            simplifiedKind = ElementKind.FIELD; //TODO: test
        }

        switch (simplifiedKind) {
            case PACKAGE:
                assert signatures.length == 1;
                return (T) jt.getElements().getPackageElement(signatures[0]);
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE: {
                assert signatures.length == 1;
                final Element type = getTypeElementByBinaryName(module, signatures[0], jt);
                if (type instanceof TypeElement) {
                    return (T) type;
                } else {
                    logger.info("Resolved type is null for kind = {}", this.kind);  // NOI18N
                }
                break;
            }
        }
        return null;
    }

    /**
     * Tests if the handle has the same signature as the parameter.
     * The handles with the same signatures are resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element}s in the different {@link javax.tools.JavaCompiler} tasks.
     * @param handle to be checked
     * @return true if the handles resolve into the same {@link Element}s
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals (@NotNull final ElementHandle<? extends Element> handle) {
        if (!isSameKind (this.kind, handle.kind) || this.signatures.length != handle.signatures.length) {
            return false;
        }
        for (int i=0; i<signatures.length; i++) {
            if (!signatures[i].equals(handle.signatures[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSameKind(ElementKind k1, ElementKind k2) {
        return (k1 == k2) ||
               (k1 == ElementKind.OTHER && (k2.isClass() || k2.isInterface())) ||
               (k2 == ElementKind.OTHER && (k1.isClass() || k1.isInterface()));
    }

    /**
     * Returns a binary name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     *
     * @return the qualified name
     * @throws IllegalStateException when this {@link ElementHandle}
     *                               isn't created for the {@link TypeElement}.
     */
    public @NotNull String getBinaryName() throws IllegalStateException {
        if ((this.kind.isClass() && isNotArray(signatures[0])) ||
            this.kind.isInterface() ||
            this.kind == ElementKind.MODULE ||
            this.kind == ElementKind.OTHER) {
            return this.signatures[0];
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns a qualified name of the {@link TypeElement} represented by this
     * {@link ElementHandle}. When the {@link ElementHandle} doesn't represent
     * a {@link TypeElement} it throws a {@link IllegalStateException}
     *
     * @return the qualified name
     * @throws IllegalStateException when this {@link ElementHandle}
     *                               isn't created for the {@link TypeElement}.
     */
    public @NotNull String getQualifiedName() throws IllegalStateException {
        if ((this.kind.isClass() && isNotArray(signatures[0])) ||
            this.kind.isInterface() ||
            this.kind == ElementKind.MODULE ||
            this.kind == ElementKind.OTHER) {
            return this.signatures[0].replace(Target.DEFAULT.syntheticNameChar(), '.');    //NOI18N
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Tests if the handle has this same signature as the parameter.
     * The handles have the same signatures if it is resolved into the same
     * element in the same {@link javax.tools.JavaCompiler} task, but may be resolved into
     * the different {@link Element} in the different {@link javax.tools.JavaCompiler} task.
     *
     * @param element to be checked
     * @return true if this handle resolves into the same {@link Element}
     * in the same {@link javax.tools.JavaCompiler} task.
     */
    public boolean signatureEquals(@NotNull final T element) {
        final ElementKind ek = element.getKind();
        final ElementKind thisKind = getKind();
        if ((ek != thisKind) && !(thisKind == ElementKind.OTHER && (ek.isClass() || ek.isInterface()))) {
            return false;
        }
        final ElementHandle<T> handle = create(element);
        return signatureEquals(handle);
    }

    /**
     * Returns the {@link ElementKind} of this element handle,
     * it is the kind of the {@link Element} from which the handle
     * was created.
     *
     * @return {@link ElementKind}
     */
    public @NotNull ElementKind getKind() {
        return this.kind;
    }

    private static final Set<ElementHandle<?>> NORMALIZATION_CACHE = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Factory method for creating {@link ElementHandle}.
     *
     * @param element for which the {@link ElementHandle} should be created. Permitted
     *                {@link ElementKind}s
     *                are: {@link ElementKind#PACKAGE}, {@link ElementKind#CLASS},
     *                {@link ElementKind#INTERFACE}, {@link ElementKind#ENUM}, {@link ElementKind#ANNOTATION_TYPE}, {@link ElementKind#METHOD},
     *                {@link ElementKind#CONSTRUCTOR}, {@link ElementKind#INSTANCE_INIT}, {@link ElementKind#STATIC_INIT},
     *                {@link ElementKind#FIELD}, and {@link ElementKind#ENUM_CONSTANT}.
     * @return a new {@link ElementHandle}
     * @throws IllegalArgumentException if the element is of an unsupported {@link ElementKind}
     */
    public static @NotNull <T extends Element> ElementHandle<T> create(@NotNull final T element) throws IllegalArgumentException {
        ElementHandle<T> eh = createImpl(element);

        boolean contains = NORMALIZATION_CACHE.contains(eh);
        if (!contains) {
            NORMALIZATION_CACHE.add(eh);
        }
        return eh;
    }

    /**
     * Creates an {@link ElementHandle} representing a {@link PackageElement}.
     *
     * @param packageName the name of the package
     * @return the created {@link ElementHandle}
     * @since 0.98
     */
    @NotNull
    public static ElementHandle<PackageElement> createPackageElementHandle(
            @NotNull final String packageName) {
        return new ElementHandle<>(ElementKind.PACKAGE, packageName);
    }

    /**
     * Creates an {@link ElementHandle} representing a {@link TypeElement}.
     *
     * @param kind       the {@link ElementKind} of the {@link TypeElement},
     *                   allowed values are {@link ElementKind#CLASS}, {@link ElementKind#INTERFACE},
     *                   {@link ElementKind#ENUM} and {@link ElementKind#ANNOTATION_TYPE}.
     * @param binaryName the class binary name as specified by JLS §13.1
     * @return the created {@link ElementHandle}
     * @throws IllegalArgumentException if kind is neither class nor interface
     * @since 0.98
     */
    @NotNull
    public static ElementHandle<TypeElement> createTypeElementHandle(
            @NotNull final ElementKind kind,
            @NotNull final String binaryName) throws IllegalArgumentException {
        if (!kind.isClass() && !kind.isInterface()) {
            throw new IllegalArgumentException(kind.toString());
        }
        return new ElementHandle<>(kind, binaryName);
    }

    /**
     * Creates an {@link ElementHandle} representing a {@link ModuleElement}.
     *
     * @param moduleName the name of the module
     * @return the created {@link ElementHandle}
     * @since 2.26
     */
    @NotNull
    public static ElementHandle<ModuleElement> createModuleElementHandle(
            @NotNull final String moduleName) {
        return new ElementHandle<>(ElementKind.MODULE, moduleName);
    }

    private static @NotNull <T extends Element> ElementHandle<T> createImpl(@NotNull final T element) throws IllegalArgumentException {
        ElementKind kind = element.getKind();
        String[] signatures;

        switch (kind) {
            case PACKAGE:
                assert element instanceof PackageElement;
                signatures = new String[]{((PackageElement) element).getQualifiedName().toString()};
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
            case RECORD:
                assert element instanceof TypeElement;
                signatures = new String[]{ClassFileUtil.encodeClassNameOrArray((TypeElement) element)};
                break;
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
                assert element instanceof ExecutableElement;
                signatures = ClassFileUtil.createExecutableDescriptor((ExecutableElement) element);
                break;
            case FIELD:
            case ENUM_CONSTANT:
            case RECORD_COMPONENT:
                assert element instanceof VariableElement;
                signatures = ClassFileUtil.createFieldDescriptor((VariableElement) element);
                break;
            case TYPE_PARAMETER:
                assert element instanceof TypeParameterElement;
                TypeParameterElement tpe = (TypeParameterElement) element;
                Element ge = tpe.getGenericElement();
                ElementKind gek = ge.getKind();
                if (gek.isClass() || gek.isInterface()) {
                    assert ge instanceof TypeElement;
                    signatures = new String[2];
                    signatures[0] = ClassFileUtil.encodeClassNameOrArray((TypeElement) ge);
                    signatures[1] = tpe.getSimpleName().toString();
                } else if (gek == ElementKind.METHOD || gek == ElementKind.CONSTRUCTOR) {
                    assert ge instanceof ExecutableElement;
                    String[] _sigs = ClassFileUtil.createExecutableDescriptor((ExecutableElement) ge);
                    signatures = new String[_sigs.length + 1];
                    System.arraycopy(_sigs, 0, signatures, 0, _sigs.length);
                    signatures[_sigs.length] = tpe.getSimpleName().toString();
                } else {
                    throw new IllegalArgumentException(gek.toString());
                }
                break;
            case MODULE:
                signatures = new String[]{((ModuleElement) element).getQualifiedName().toString()};
                break;
            default:
                throw new IllegalArgumentException(kind.toString());
        }
        return new ElementHandle<T>(kind, signatures);
    }

//    /**
//     * Gets {@link ElementHandle} from {@link TypeMirrorHandle} representing {@link DeclaredType}.
//     * @param typeMirrorHandle from which the {@link ElementHandle} should be retrieved. Permitted
//     * {@link TypeKind} is {@link TypeKind#DECLARED}.
//     * @return an {@link ElementHandle}
//     * @since 0.29.0
//     */
//    public static @NotNull ElementHandle<? extends TypeElement> from (@NotNull final TypeMirrorHandle<? extends DeclaredType> typeMirrorHandle) {
//        Parameters.notNull("typeMirrorHandle", typeMirrorHandle);
//        if (typeMirrorHandle.getKind() != TypeKind.DECLARED) {
//            throw new IllegalStateException("Incorrect kind: " + typeMirrorHandle.getKind());
//        }
//        return (ElementHandle<TypeElement>)typeMirrorHandle.getElementHandle();
//    }

    public @Override String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(this.getClass().getSimpleName());
        result.append('[');                                // NOI18N
        result.append("kind=").append(this.kind.toString());      // NOI18N
        result.append("; sigs=");                          // NOI18N
        for (String sig : this.signatures) {
            result.append(sig);
            result.append(' ');                            // NOI18N
        }
        result.append(']');                                // NOI18N
        return result.toString();
    }

    @Override
    public int hashCode () {
        int hashCode = 0;

        for (String sig : signatures) {
            hashCode = hashCode ^ (sig != null ? sig.hashCode() : 0);
        }

        return hashCode;
    }

    /**{@inheritDoc}*/
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals (Object other) {
        if (other instanceof ElementHandle) {
            return signatureEquals((ElementHandle)other);
        }
        return false;
    }

    /**
     * Returns the element signature.
     * Package private, used by ClassIndex.
     */
    String[] getSignature () {
        return this.signatures;
    }

    private static Element getTypeElementByBinaryName (final ModuleElement module, final String signature, final JavacTaskImpl jt) {
        logger.trace("getTypeElementByBinaryName: signature = {}", signature);
        if (isNone(signature)) {
            return Symtab.instance(jt.getContext()).noSymbol;
        }
        else if (!isNotArray(signature)) {
            return Symtab.instance(jt.getContext()).arrayClass;
        }
        else {
            return module != null
                    ? ElementUtils.getTypeElementByBinaryName(jt, module, signature)
                    : ElementUtils.getTypeElementByBinaryName(jt, signature);
        }
    }

    private static boolean isNone (String signature) {
        return signature.isEmpty();
    }

    private static boolean isNotArray(String signature) {
        return signature.length() != 1 || signature.charAt(0) != '[';
    }
}
