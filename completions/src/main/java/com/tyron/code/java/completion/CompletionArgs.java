package com.tyron.code.java.completion;

import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.java.parsing.PositionContext;
import com.tyron.code.project.model.module.JavaModule;
import shadow.com.sun.source.util.TreePath;

public record CompletionArgs(@Deprecated PositionContext positionContext, JavaModule module, TreePath currentAnalyzedPath, AnalysisResult analysisResult, String prefix) {
}
