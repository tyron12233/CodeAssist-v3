package com.tyron.code.java.completion;

import shadow.javax.lang.model.element.Element;

public abstract class ElementBasedCompletionCandidate<T extends Element> implements CompletionCandidate{

    private final T element;

    ElementBasedCompletionCandidate(T element) {
        this.element = element;
    }

    T getElement() {
        return element;
    }
}
