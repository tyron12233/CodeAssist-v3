package com.tyron.code.java.completion;

import com.tyron.code.java.model.ResolveAction;
import com.tyron.code.java.model.ResolveActionParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class CompletionCandidateDecorator implements CompletionCandidate {


    public static class Builder {
        private final CompletionCandidate candidate;
        private String name;
        private final Map<ResolveAction, ResolveActionParams> resolveActions;

        public Builder(CompletionCandidate candidate) {
            this.candidate = candidate;
            resolveActions = new HashMap<>(candidate.getResolveActions());
            name = candidate.getName();
        }

        public Builder withResolveAction(ResolveAction action, ResolveActionParams params) {
            resolveActions.put(action, params);
            return this;
        }

        public CompletionCandidate build() {
            return new CompletionCandidateDecorator(
                    name,
                    candidate.getNameDescription().orElse(null),
                    candidate.getDetail().orElse(null),
                    candidate.getInsertPlainText(TextEditOptions.DEFAULT).orElse(null),
                    candidate.getInsertSnippet(TextEditOptions.DEFAULT).orElse(null),
                    candidate.getKind(),
                    candidate.getSortCategory(),
                    resolveActions
            );
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }
    }

    public static Builder builder(CompletionCandidate candidate) {
        return new Builder(candidate);
    }

    private final String name;

    private final String nameDescription;
    private final String detail;
    private final String insertPlainText;
    private final String insertSnippet;
    private final Kind kind;

    private final SortCategory sortCategory;

    private final Map<ResolveAction, ResolveActionParams> resolveActions;

    public CompletionCandidateDecorator(String name,
                                        String nameDescription,
                                        String detail,
                                        String insertPlainText,
                                        String insertSnippet,
                                        Kind kind,
                                        SortCategory sortCategory,
                                        Map<ResolveAction, ResolveActionParams> resolveActions) {
        this.name = name;
        this.nameDescription = nameDescription;
        this.detail = detail;
        this.insertPlainText = insertPlainText;
        this.insertSnippet = insertSnippet;
        this.kind = kind;
        this.sortCategory = sortCategory;
        this.resolveActions = resolveActions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public SortCategory getSortCategory() {
        return sortCategory;
    }

    @Override
    public Optional<String> getNameDescription() {
        return Optional.ofNullable(nameDescription);
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }

    @Override
    public Optional<String> getInsertPlainText(TextEditOptions textEditOptions) {
        return Optional.ofNullable(insertPlainText);
    }

    @Override
    public Optional<String> getInsertSnippet(TextEditOptions textEditOptions) {
        return Optional.ofNullable(insertSnippet);
    }

    @Override
    public Map<ResolveAction, ResolveActionParams> getResolveActions() {
        return resolveActions;
    }
}
