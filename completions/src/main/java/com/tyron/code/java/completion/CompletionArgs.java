package com.tyron.code.java.completion;

import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.java.parsing.PositionContext;
import com.tyron.code.project.model.ProjectModule;
import shadow.com.sun.source.util.TreePath;

public record CompletionArgs(@Deprecated PositionContext positionContext, ProjectModule module, TreePath currentAnalyzedPath, AnalysisResult analysisResult, String prefix) {
}
