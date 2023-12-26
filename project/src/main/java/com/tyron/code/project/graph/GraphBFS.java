package com.tyron.code.project.graph;

import java.util.*;

public abstract class GraphBFS<T> {

    private final T startingNode;

    public GraphBFS(T startingNode) {
        this.startingNode = startingNode;
    }

    public void traverse(NodeVisitor<T> visitor) {
        Deque<T> queue = new LinkedList<>();
        Set<T> visitedSet = new HashSet<>();

        queue.addLast(startingNode);
        visitedSet.add(startingNode);

        while (!queue.isEmpty()) {
            T currentNode = queue.removeFirst();
            visitor.visit(currentNode);

            for (T childNode : getChildren(currentNode)) {
                if (!visitedSet.contains(childNode)) {
                    queue.addLast(childNode);
                    visitedSet.add(childNode);
                }
            }
        }
    }

    protected abstract Collection<T> getChildren(T node);
}
