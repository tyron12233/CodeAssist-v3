package com.tyron.code.java.completion;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class CompletionPrefixUtils {
    public static boolean prefixPartiallyMatch(String partial, String string) {
        // empty prefix means we need to match all
        if (partial.isEmpty()) {
            return true;
        }
        return FuzzySearch.partialRatio(partial, string) >= 85;
    }
}
