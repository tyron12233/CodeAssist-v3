package com.tyron.code.info.builder;


import com.tyron.code.info.ClassInfo;
import com.tyron.code.info.properties.BasicPropertyContainer;
import com.tyron.code.info.properties.PropertyContainer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/* Common builder info for {@link ClassInfo}.
 *
 * @param <B>
 * 		Self type. Exists so implementations don't get stunted in their chaining.
 */
public abstract class AbstractClassInfoBuilder<B extends AbstractClassInfoBuilder<?>> {

    private String name;
    private String superName = "java/lang/Object";
    private List<String> interfaces = Collections.emptyList();

    private String signature;
    private String sourceFileName;

    private String outerClassName;
    private String outerMethodName;
    private String outerMethodDescriptor;
    private PropertyContainer propertyContainer = new BasicPropertyContainer();

    protected AbstractClassInfoBuilder() {
        // default
    }

    protected AbstractClassInfoBuilder(ClassInfo classInfo) {
        // copy state
        withName(classInfo.getName());
        withSuperName(classInfo.getSuperName());
        withInterfaces(classInfo.getInterfaces());
//        withAccess(classInfo.getAccess());
        withSignature(classInfo.getSignature());
        withSourceFileName(classInfo.getSourceFileName());
//        withAnnotations(classInfo.getAnnotations());
//        withTypeAnnotations(classInfo.getTypeAnnotations());
        withOuterClassName(classInfo.getOuterClassName());
        withOuterMethodName(classInfo.getOuterMethodName());
        withOuterMethodDescriptor(classInfo.getOuterMethodDescriptor());
//        withInnerClasses(classInfo.getInnerClasses());
//        withFields(classInfo.getFields());
//        withMethods(classInfo.getMethods());
        withPropertyContainer(new BasicPropertyContainer(classInfo.getPersistentProperties()));
    }

    @SuppressWarnings("unchecked")
    public B withPropertyContainer(PropertyContainer propertyContainer) {
        this.propertyContainer = Objects.requireNonNullElseGet(propertyContainer, BasicPropertyContainer::new);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withName(String name) {
        this.name = name;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withSuperName(String superName) {
        this.superName = superName;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withInterfaces(List<String> interfaces) {
        if (interfaces == null)
            this.interfaces = Collections.emptyList();
        else
            this.interfaces = interfaces;
        return (B) this;
    }

//    @SuppressWarnings("unchecked")
//    public B withAccess(int access) {
//        this.access.value = access;
//        return (B) this;
//    }

    @SuppressWarnings("unchecked")
    public B withSignature(String signature) {
        this.signature = signature;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOuterClassName(String outerClassName) {
        this.outerClassName = outerClassName;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOuterMethodName(String outerMethodName) {
        this.outerMethodName = outerMethodName;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B withOuterMethodDescriptor(String outerMethodDescriptor) {
        this.outerMethodDescriptor = outerMethodDescriptor;
        return (B) this;
    }

    public String getName() {
        return name;
    }

    public String getSuperName() {
        return superName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getSignature() {
        return signature;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getOuterClassName() {
        return outerClassName;
    }

    public String getOuterMethodName() {
        return outerMethodName;
    }

    public String getOuterMethodDescriptor() {
        return outerMethodDescriptor;
    }

    public PropertyContainer getPropertyContainer() {
        return propertyContainer;
    }

    protected void verify() {

    }

    public abstract ClassInfo build();
}
