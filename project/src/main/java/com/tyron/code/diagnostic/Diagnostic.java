package com.tyron.code.diagnostic;

public record Diagnostic(Kind kind, String message, int start, int end, int line, int column) {

    public static Diagnostic from(shadow.javax.tools.Diagnostic<?> diagnostic) {
        return new Diagnostic(
                from(diagnostic.getKind()),
                diagnostic.getMessage(null),
                (int) diagnostic.getStartPosition(),
                (int) diagnostic.getEndPosition(),
                (int) diagnostic.getLineNumber(),
                (int) diagnostic.getColumnNumber()
        );
    }

    private static Kind from(shadow.javax.tools.Diagnostic.Kind kind) {
        return switch (kind) {
            case ERROR -> Kind.ERROR;
            case WARNING -> Kind.WARNING;
            case MANDATORY_WARNING -> Kind.MANDATORY_WARNING;
            case NOTE -> Kind.NOTE;
            default -> Kind.OTHER;
        };
    }

    public enum Kind {
        ERROR,
        WARNING,
        MANDATORY_WARNING,
        NOTE,
        OTHER,
        ;
    }
}
