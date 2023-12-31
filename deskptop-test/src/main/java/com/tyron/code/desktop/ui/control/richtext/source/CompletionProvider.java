package com.tyron.code.desktop.ui.control.richtext.source;

import com.tyron.code.desktop.ui.control.richtext.Editor;

import java.util.List;

public interface CompletionProvider {
    List<String> getCompletionSuggestions(Editor editor);
}
