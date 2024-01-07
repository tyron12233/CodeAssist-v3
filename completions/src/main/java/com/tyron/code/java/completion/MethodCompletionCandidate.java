package com.tyron.code.java.completion;

import com.tyron.code.java.util.PrintUtil;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.type.ExecutableType;
import shadow.javax.lang.model.type.TypeKind;

import java.util.Optional;

public class MethodCompletionCandidate implements CompletionCandidate {
    private final ExecutableElement element;
    private final ExecutableType type;

    public MethodCompletionCandidate(ExecutableElement element, ExecutableType type) {
        this.element = element;
        this.type = type;
    }

    @Override
    public String getName() {
        return element.getSimpleName().toString();
    }

    @Override
    public Optional<String> getNameDescription() {
        return Optional.of(PrintUtil.printMethodType(type));
    }

    @Override
    public Optional<String> getInsertSnippet(TextEditOptions textEditOptions) {
        StringBuilder builder = new StringBuilder();
        builder.append(element.getSimpleName());
        builder.append("(");
        if (!element.getParameters().isEmpty()) {
            builder.append("$0");
        }
        builder.append(")");

        if (element.getReturnType().getKind() == TypeKind.VOID) {
            builder.append(";"); // Add semicolon if return type is void
        }
        return Optional.of(builder.toString());
    }


    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.empty();
    }
}
