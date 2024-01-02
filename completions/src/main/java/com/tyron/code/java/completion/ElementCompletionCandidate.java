package com.tyron.code.java.completion;

import shadow.javax.lang.model.element.Element;
import shadow.javax.lang.model.element.ElementKind;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.element.TypeElement;
import shadow.javax.lang.model.type.TypeMirror;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ElementCompletionCandidate extends ElementBasedCompletionCandidate<Element> {

    private final Map<String, Object> objectsMap = new HashMap<>();

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

    public boolean getBoolean(String name) {
        Object o = objectsMap.get(name);
        if (o == null) {
            return false;
        }
        return (boolean) o;
    }

    public String getString(String name) {
        return (String) objectsMap.get(name);
    }

    public <T> void putData(String name, T value) {
        objectsMap.put(name, value);
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
