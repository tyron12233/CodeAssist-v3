package com.tyron.code.project.graph;

import com.google.common.graph.Graph;

@SuppressWarnings("UnstableApiUsage")
public class GraphPrinter {

    public static <T> void printGraphAsTree(Graph<T> graph, T root, String prefix) {
        if (!graph.nodes().contains(root)) {
            System.err.println("Node '" + root + "' not found!");
            return;
        }

        System.out.println(prefix + root);

        for (T child : graph.successors(root)) {
            printGraphAsTree(graph, child, prefix + (isLastChild(graph, root, child) ? "   " : "|  "));
        }
    }

    private static <T> boolean isLastChild(Graph<T> graph, T parent, T child) {
        // Check if there are no more successors of the parent after this child
        return graph.successors(parent).stream().noneMatch(node -> node.equals(child));
    }
}
