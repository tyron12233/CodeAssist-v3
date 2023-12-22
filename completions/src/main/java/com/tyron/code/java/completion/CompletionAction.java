package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.java.parsing.PositionContext;

/** Action to perform the requested completion. */
interface CompletionAction {
    ImmutableList<CompletionCandidate> getCompletionCandidates(CompletionArgs args);
}