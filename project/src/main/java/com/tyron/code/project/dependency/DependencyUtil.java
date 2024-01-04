package com.tyron.code.project.dependency;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.tyron.code.project.ModuleManager;
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

            List<String> dependencies = current.getDependencies();
            for (String dependencyName : dependencies) {
                ModuleManager moduleManager = module.getModuleManager();
                Optional<Module> dependency = moduleManager.findModuleByName(dependencyName);
                if (dependency.isEmpty()) {
                    throw new IllegalStateException("Module " + current.getName() + " depends on module " + dependencyName + " which does not exist");
                }
                if (!visitedModules.contains(dependency.get())) {
                    graph.putEdge(current, dependency.get());
                    queue.add(dependency.get());
                }
            }
        }

        return graph;
    }
}
