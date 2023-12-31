package com.tyron.code.info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Outline of a compiled class file.
 */
public interface ClassInfo extends Info {

    @Nullable
    String getSourceFileName();

    @NotNull
    List<String> getInterfaces();

    @Nullable
    String getSuperName();

    @NotNull
    default String getSimpleName() {
        String className = getName();
        int packageIndex = className.lastIndexOf('/');
        if (packageIndex <= 0) {
            return className;
        }
        return className.substring(packageIndex + 1);
    }

    @Nullable
    default String getPackageName() {
        String className = getName();
        int packageIndex = className.lastIndexOf('/');
        if (packageIndex <= 0) {
            return null;
        }
        return className.substring(0, packageIndex);
    }

    default String[] getPackageNameParts() {
        String packageName = getPackageName();
        if (packageName == null) {
            return new String[0];
        }
        return packageName.split("/");
    }

    default boolean isInDefaultPackage() {
        return getPackageName() == null;
    }

    String getSignature();

    /**
     * @return Name of outer class that this is declared in, if this is an inner class.
     * {@code null} when this class is not an inner class.
     */
    @Nullable
    String getOuterClassName();

    /**
     * @return Name of the outer method that this is declared in, as an anonymous inner class.
     * {@code null} when this class is not an inner anonymous class.
     *
     * @see #getOuterMethodDescriptor() Descriptor of outer method
     */
    @Nullable
    String getOuterMethodName();

    /**
     * @return Descriptor of the outer method that this is declared in, as an anonymous inner class.
     * {@code null} when this class is not an inner anonymous class.
     *
     * @see #getOuterMethodName() Name of outer method.
     */
    @Nullable
    String getOuterMethodDescriptor();


}
