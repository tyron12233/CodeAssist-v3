package com.tyron.code.java.completion;

import shadow.javax.lang.model.element.Element;

public abstract class ElementBasedCompletionCandidate implements CompletionCandidate{

    private final Element element;

    ElementBasedCompletionCandidate(Element element) {
        this.element = element;
    }

    Element getElement() {
        return element;
    }
}
