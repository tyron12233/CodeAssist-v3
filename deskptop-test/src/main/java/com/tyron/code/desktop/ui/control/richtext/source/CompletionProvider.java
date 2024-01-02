package com.tyron.code.desktop.ui.control.richtext.source;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.java.completion.CompletionResult;

import java.util.List;

public interface CompletionProvider {
    CompletionResult getCompletionSuggestions(Editor editor);
}
