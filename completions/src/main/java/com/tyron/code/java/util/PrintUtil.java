package com.tyron.code.java.util;

import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.type.ExecutableType;
import shadow.javax.lang.model.type.TypeMirror;

import java.util.stream.Collectors;

public class PrintUtil {

    public static String printMethodParameters(ExecutableElement method) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        var params = method.getParameters().stream()
                .map(param -> getSimpleName(param.asType()) + " " + param.getSimpleName().toString())
                .collect(Collectors.joining(", "));
        builder.append(params);
        builder.append(")");
        return builder.toString();
    }

    public static String getSimpleName(TypeMirror typeMirror) {
        return getSimpleName(typeMirror.toString());
    }

    public static String getSimpleName(String fullyQualifiedName) {
        int index = fullyQualifiedName.lastIndexOf(".");
        if (index == -1) {
            return fullyQualifiedName;
        }
        return fullyQualifiedName.substring(index + 1);
    }

    public static String printMethodType(ExecutableType type) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        var params = type.getParameterTypes().stream()
                .map(param -> getSimpleName(param) + " " + suggestName(param))
                .collect(Collectors.joining(", "));
        builder.append(params);
        builder.append(")");
        builder.append(" -> ");
        builder.append(getSimpleName(type.getReturnType()));
        return builder.toString();
    }

    public static String suggestName(TypeMirror type) {
        String name = getSimpleName(type);
        if (name.length() > 1) {
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        }
        return name.toLowerCase();
    }
}
