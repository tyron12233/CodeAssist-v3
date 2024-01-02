package com.tyron.code.java.completion;

import com.tyron.code.java.util.PrintUtil;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.element.VariableElement;
import shadow.javax.lang.model.type.TypeKind;
import shadow.javax.lang.model.type.TypeMirror;

import java.util.List;
import java.util.Optional;

public class ExecutableElementCompletionCandidate extends ElementBasedCompletionCandidate<ExecutableElement> {

    ExecutableElementCompletionCandidate(ExecutableElement element) {
        super(element);
    }

    @Override
    public String getName() {
        return getElement().getSimpleName().toString();
    }

    @Override
    public Optional<String> getNameDescription() {
        return Optional.of(PrintUtil.printMethodParameters(getElement()));
    }

    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.of(PrintUtil.getSimpleName(getElement().getReturnType()));
    }
}
