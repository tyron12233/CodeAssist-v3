package com.tyron.code.project.graph;

public interface NodeVisitor<T> {


    void visit(T currentNode);
}
