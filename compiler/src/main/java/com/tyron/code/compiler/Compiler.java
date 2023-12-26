//package com.tyron.code.compiler;
//
//import com.google.common.graph.Graph;
//import com.google.common.graph.GraphBuilder;
//import com.google.common.graph.MutableGraph;
//import com.tyron.code.project.graph.GraphPrinter;
//import com.tyron.code.project.model.v2.JavaModule;
//
//import java.util.*;
//import java.util.function.Consumer;
//
//@SuppressWarnings("UnstableApiUsage")
//public class Compiler {
//
//    public static void main(String[] args) {
//        JavaModule projectModule = new ProjectModule("root");
//
//        ProjectModule guava = new ProjectModule("guava");
//        projectModule.addImplementationDependency(guava);
//
//        ProjectModule annotations = new ProjectModule("annotations");
//        guava.addImplementationDependency(annotations);
//
//        ProjectModule googleBase = new ProjectModule("googleBase");
//        guava.addImplementationDependency(googleBase);
//        annotations.addImplementationDependency(googleBase);
//
//        MutableGraph<Module> moduleMutableGraph = resolveProjectOutput(projectModule);
//        GraphPrinter.printGraphAsTree(moduleMutableGraph, projectModule, "");
//
//
//        Map<Module, String> projectOutputMap = new HashMap<>();
//        Set<Module> visited = new HashSet<>();
//        processDependencies(moduleMutableGraph, projectModule, module -> {
//            if (visited.contains(module)) {
//                return;
//            }
//            System.out.println("Processing: " + module.getDebugName());
//
//            ProjectModule project = ((ProjectModule) module);
//            List<Module> dependencies = project.getDependingModules(DependencyType.COMPILE_TIME);
//            for (Module dependency : dependencies) {
//                if (dependency.getModuleType() == ModuleType.PROJECT) {
//                    ProjectModule projectDependency = ((ProjectModule) dependency);
//
//                    String output = projectOutputMap.get(projectDependency);
//                    System.out.println("L-- Using output: " + output);
//                }
//            }
//
//            projectOutputMap.put(module, "O: " + module.getDebugName());
//            visited.add(module);
//        });
//    }
//
//
//    public static void processDependencies(Graph<Module> graph, Module module, Consumer<Module> processModule) {
//        if (!graph.nodes().contains(module)) {
//            System.err.println("Module '" + module + "' not found!");
//            return;
//        }
//
//        Stack<Module> stack = new Stack<>();
//        Set<Module> visited = new HashSet<>();
//
//        stack.push(module);
//
//        while (!stack.isEmpty()) {
//            Module current = stack.pop();
//
//            if (visited.contains(current)) {
//                continue;
//            }
//
//            visited.add(current);
//
//            for (Module dependency : graph.successors(current)) {
//                processDependencies(graph, dependency, processModule);
//            }
//
//            processModule.accept(current);
//        }
//    }
//
//    public static MutableGraph<Module> resolveProjectOutput(ProjectModule module) {
//        MutableGraph<Module> graph = GraphBuilder.directed()
//                .allowsSelfLoops(false)
//                .build();
//
//        Deque<ProjectModule> queue = new LinkedList<>();
//        Set<Module> visitedModules = new HashSet<>();
//        queue.addLast(module);
//
//        while (!queue.isEmpty()) {
//            ProjectModule current = queue.removeFirst();
//
//            graph.addNode(current);
//
//            visitedModules.add(current);
//
//            List<Module> dependencies = current.getDependingModules(DependencyType.COMPILE_TIME);
//            for (Module dependency : dependencies) {
//                if (dependency.getModuleType() == ModuleType.PROJECT) {
//                    if (!visitedModules.contains(dependency)) {
//                        graph.putEdge(current, dependency);
//                        queue.add((ProjectModule) dependency);
//                    }
//                }
//            }
//        }
//
//        return graph;
//    }
//}
