package com.tyron.code.info;

import com.tyron.code.info.builder.AbstractClassInfoBuilder;
import com.tyron.code.info.properties.Property;
import com.tyron.code.info.properties.PropertyContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BasicClassInfo implements ClassInfo {

    private final PropertyContainer properties;
    private final String name;
    private final String superName;
    private final List<String> interfaces;
    private final int access;
    private final String signature;
    private final String sourceFileName;
    //    private final List<AnnotationInfo> annotations;
//    private final List<TypeAnnotationInfo> typeAnnotations;
    private final String outerClassName;
    private final String outerMethodName;
    private final String outerMethodDescriptor;
//    private final List<InnerClassInfo> innerClasses;
//    private final List<FieldMember> fields;
//    private final List<MethodMember> methods;
//    private List<String> breadcrumbs;

    protected BasicClassInfo(AbstractClassInfoBuilder<?> builder) {
        this(builder.getName(),
                builder.getSuperName(),
                builder.getInterfaces(),
//                builder.getAccess(),
                builder.getSignature(),
                builder.getSourceFileName(),
//                builder.getAnnotations(),
//                builder.getTypeAnnotations(),
                builder.getOuterClassName(),
                builder.getOuterMethodName(),
                builder.getOuterMethodDescriptor(),
//                builder.getInnerClasses(),
//                builder.getFields(),
//                builder.getMethods(),
                builder.getPropertyContainer());
    }

    public BasicClassInfo(String name, String superName, List<String> interfaces, String signature, String sourceFileName, String outerClassName, String outerMethodName, String outerMethodDescriptor, PropertyContainer propertyContainer) {
        this.name = name;
        this.superName = superName;
        this.interfaces = interfaces;
        this.signature = signature;
        this.sourceFileName = sourceFileName;
        this.outerClassName = outerClassName;
        this.outerMethodName = outerMethodName;
        this.outerMethodDescriptor = outerMethodDescriptor;
        this.properties = propertyContainer;
        this.access = 0;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getSuperName() {
        return superName;
    }

    @Nonnull
    @Override
    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getSignature() {
        return signature;
    }

    @Nullable
    @Override
    public String getSourceFileName() {
        return sourceFileName;
    }

    @Nullable
    @Override
    public String getOuterClassName() {
        return outerClassName;
    }

    @Nullable
    @Override
    public String getOuterMethodDescriptor() {
        return outerMethodDescriptor;
    }

    @Nullable
    @Override
    public String getOuterMethodName() {
        return outerMethodName;
    }

    @Override
    public <V> void setProperty(Property<V> property) {
        properties.setProperty(property);
    }

    @Override
    public void removeProperty(String key) {
        properties.removeProperty(key);
    }

    @Nonnull
    @Override
    public @NotNull Map<String, Property<?>> getProperties() {
        return properties.getProperties();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicClassInfo other = (BasicClassInfo) o;

        // NOTE: Do NOT consider the properties since contents of the map can point back to this instance
        //       or our containing resource, causing a cycle.
        if (access != other.access) return false;
        if (!name.equals(other.name)) return false;
        if (!Objects.equals(superName, other.superName)) return false;
        if (!interfaces.equals(other.interfaces)) return false;
        if (!Objects.equals(signature, other.signature)) return false;
        if (!Objects.equals(sourceFileName, other.sourceFileName)) return false;
//        if (!annotations.equals(other.annotations)) return false;
//        if (!typeAnnotations.equals(other.typeAnnotations)) return false;
        if (!Objects.equals(outerClassName, other.outerClassName)) return false;
        if (!Objects.equals(outerMethodName, other.outerMethodName)) return false;
        if (!Objects.equals(outerMethodDescriptor, other.outerMethodDescriptor)) return false;
//        if (!innerClasses.equals(other.innerClasses)) return false;
//        if (!fields.equals(other.fields)) return false;
//        return methods.equals(other.methods);
        return true;
    }


    @Override
    public int hashCode() {
        // NOTE: Do NOT consider the properties since contents of the map can point back to this instance
        //       or our containing resource, causing a cycle.
        int result =  name.hashCode();
        result = 31 * result + (superName != null ? superName.hashCode() : 0);
        result = 31 * result + interfaces.hashCode();
        result = 31 * result + access;
        result = 31 * result + (signature != null ? signature.hashCode() : 0);
        result = 31 * result + (sourceFileName != null ? sourceFileName.hashCode() : 0);
//        result = 31 * result + annotations.hashCode();
//        result = 31 * result + typeAnnotations.hashCode();
        result = 31 * result + (outerClassName != null ? outerClassName.hashCode() : 0);
        result = 31 * result + (outerMethodName != null ? outerMethodName.hashCode() : 0);
        result = 31 * result + (outerMethodDescriptor != null ? outerMethodDescriptor.hashCode() : 0);
//        result = 31 * result + innerClasses.hashCode();
//        result = 31 * result + fields.hashCode();
//        result = 31 * result + methods.hashCode();
        return result;
    }

}
