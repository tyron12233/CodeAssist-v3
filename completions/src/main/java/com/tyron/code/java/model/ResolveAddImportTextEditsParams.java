package com.tyron.code.java.model;

import java.net.URI;

/**
 * Resolve data for ADD_IMPORT_TEXT_EDIT action.
 */
public class ResolveAddImportTextEditsParams implements ResolveActionParams {

    /**
     * The text document's URI.
     */
    public URI uri;
    /**
     * The full name of the class to be imported.
     */
    public String classFullName;

    public ResolveAddImportTextEditsParams() {
    }

    public ResolveAddImportTextEditsParams(URI uri, String classFullName) {
        this.uri = uri;
        this.classFullName = classFullName;
    }
}