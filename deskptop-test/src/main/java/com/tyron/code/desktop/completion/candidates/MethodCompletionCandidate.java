package com.tyron.code.desktop.completion.candidates;

import org.fife.ui.autocomplete.AbstractCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import javax.swing.*;

public class MethodCompletionCandidate extends AbstractCompletion {

    private final String replacementText;
    private final String summary;


    protected MethodCompletionCandidate(CompletionProvider provider, Icon icon, String replacementText, String summary) {
        super(provider, icon);
        this.replacementText = replacementText;
        this.summary = summary;
    }

    public static MethodCompletionCandidate create(CompletionProvider provider, String replacementText, String summary) {
        return new MethodCompletionCandidate(provider, null, replacementText, summary);
    }

    @Override
    public String getReplacementText() {
        return replacementText;
    }

    @Override
    public String getSummary() {
        return summary;
    }
}
