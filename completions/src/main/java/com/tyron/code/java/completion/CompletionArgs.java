package com.tyron.code.java.completion;

import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.java.parsing.PositionContext;
import shadow.com.sun.source.util.TreePath;

public record CompletionArgs(PositionContext positionContext, TreePath currentAnalyzedPath, AnalysisResult analysisResult, String prefix) {
}
