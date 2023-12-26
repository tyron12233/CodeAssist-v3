package com.tyron.code.project.util;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassNameUtils {

    public static String getPackageOnly(String fqn) {
        if (fqn.endsWith(".class")) {
            fqn = fqn.substring(0, fqn.length() - ".class".length());
        }

        String className = getClassName(fqn);
        return fqn.substring(0, fqn.length() - className.length() - 1);
    }

    /**
     * Returns the Fully Qualified Name of a classpath
     */
    @VisibleForTesting
    public static String getFqn(String path) {
        String classPath = path.replace("/", ".");
        if (classPath.startsWith(".")) {
            classPath = classPath.substring(1);
        }
        return classPath.substring(0, classPath.length() - ".class".length());
    }

    @VisibleForTesting
    public static String getClassName(String fqn) {
        if (!fqn.contains(".")) {
            return fqn;
        }

        return fqn.substring(fqn.lastIndexOf('.') + 1);
    }

    public static List<String> getAsQualifierList(String className) {
        List<String> qualifiers = Arrays.stream(className.split("\\.")).toList();
        if (qualifiers.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> validQualifiers = new ArrayList<>(qualifiers.size());
        for (String qualifier : qualifiers) {
            if (qualifier.isEmpty()) {
                continue;
            }

            validQualifiers.add(qualifier);
        }
        return validQualifiers;
    }

}
