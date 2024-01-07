package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableMap;
import com.tyron.code.java.model.ResolveAction;
import com.tyron.code.java.model.ResolveActionParams;
import com.tyron.code.java.model.ResolveAddImportTextEditsParams;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/** A candidate with import class edit actions. */
class ClassForImportCandidate implements CompletionCandidate {
    private final String fqn;
    private final String simpleName;
    private final Path filePath;

    ClassForImportCandidate(String fqn, String simpleName, String filePath) {
        this.fqn = fqn;
        this.simpleName = simpleName;
        this.filePath = Paths.get(filePath);
    }

    @Override
    public String getName() {
        return simpleName;
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

    @Override
    public Optional<String> getDetail() {
        return Optional.of(fqn);
    }

    @Override
    public SortCategory getSortCategory() {
        return SortCategory.TO_IMPORT;
    }

    @Override
    public Map<ResolveAction, ResolveActionParams> getResolveActions() {
        ImmutableMap.Builder<ResolveAction, ResolveActionParams> builder = new ImmutableMap.Builder<>();
        ResolveAddImportTextEditsParams params = new ResolveAddImportTextEditsParams();
        params.uri = filePath.toUri();
        params.classFullName = fqn;
        builder.put(ResolveAction.ADD_IMPORT_TEXT_EDIT, params);
        return builder.build();
    }

    @Override
    public String toString() {
        return "ClassForImportCandidate{" +
                "filePath=" + filePath +
                '}';
    }
}