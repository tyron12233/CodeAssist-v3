package com.tyron.code.java.completion;

import shadow.javax.lang.model.element.Element;
import shadow.javax.lang.model.element.ElementKind;

import java.util.Optional;

public class ElementCompletionCandidate extends ElementBasedCompletionCandidate {

    ElementCompletionCandidate(Element element) {
        super(element);
    }

    @Override
    public String getName() {
        return getElement().getSimpleName().toString();
    }

    @Override
    public Kind getKind() {
        return toCandidateKind(getElement().getKind());
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.empty();
    }

    public static Kind toCandidateKind(ElementKind elementKind) {
        return switch (elementKind) {
            case CLASS -> Kind.CLASS;
            case ANNOTATION_TYPE, INTERFACE -> Kind.INTERFACE;
            case ENUM -> Kind.ENUM;
            case METHOD -> Kind.METHOD;
            case PARAMETER, LOCAL_VARIABLE -> Kind.VARIABLE;
            case FIELD -> Kind.FIELD;
            case PACKAGE -> Kind.PACKAGE;
            default -> Kind.UNKNOWN;
        };
    }
}
