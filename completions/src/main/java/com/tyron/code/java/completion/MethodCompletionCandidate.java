package com.tyron.code.java.completion;

import com.tyron.code.java.util.PrintUtil;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.type.ExecutableType;

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
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.empty();
    }
}
