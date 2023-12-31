package com.tyron.code.project.dependency;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.tyron.code.project.model.module.Module;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class DependencyUtil {

    public static MutableGraph<Module> buildDependencyGraph(Module module) {
        MutableGraph<Module> graph = GraphBuilder.directed()
                .allowsSelfLoops(false)
                .build();

        Deque<Module> queue = new LinkedList<>();
        Set<Module> visitedModules = new HashSet<>();
        queue.addLast(module);

        while (!queue.isEmpty()) {
            Module current = queue.removeFirst();

            graph.addNode(current);

            visitedModules.add(current);

            List<Module> dependencies = current.getDependencies();
            for (Module dependency : dependencies) {
                if (!visitedModules.contains(dependency)) {
                    graph.putEdge(current, dependency);
                    queue.add(dependency);
                }
            }
        }

        return graph;
    }
}
