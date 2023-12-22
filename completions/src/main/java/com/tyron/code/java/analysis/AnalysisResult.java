package com.tyron.code.java.analysis;

import shadow.com.sun.source.tree.CompilationUnitTree;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.javax.lang.model.element.Element;

public record AnalysisResult(JavacTaskImpl javacTask, CompilationUnitTree parsedTree, Iterable<? extends Element> analyzed) {

}