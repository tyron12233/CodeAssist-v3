package com.tyron.code.java.completion;

import java.util.Optional;

public class SimpleCompletionCandidate implements CompletionCandidate{

    private final String name;

    public SimpleCompletionCandidate(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Kind getKind() {
        return Kind.UNKNOWN;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.empty();
    }
}
