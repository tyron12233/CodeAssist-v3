//package com.tyron.code.java.completion.model;
//
//import com.google.common.graph.Graph;
//import com.google.common.graph.GraphBuilder;
//import com.google.common.graph.MutableGraph;
//import com.tyron.code.java.completion.CompletionResult;
//import com.tyron.code.java.completion.Completor;
//import com.tyron.code.project.impl.FileSystemModuleManager;
//import com.tyron.code.project.model.Module;
//import com.tyron.code.project.util.ModuleUtils;
//import org.junit.jupiter.api.Test;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.function.Consumer;
//
//class ModuleTest {
//
//    @Test
//    void getOrCreatePackage() {
//        Path root = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/lib");
//        FileSystemModuleManager manager = new FileSystemModuleManager(root);
//
//        manager.initialize();
//
//        ProjectModule projectModule = manager.getProjectModule();
//
//        ProjectModule secondModule = new ProjectModule("second");
//        projectModule.addImplementationDependency(secondModule);
//
//        ProjectModule thirdModule = new ProjectModule("third");
//        projectModule.addImplementationDependency(thirdModule);
//
//        ProjectModule fourthModule = new ProjectModule( "fourth");
//        thirdModule.addImplementationDependency(fourthModule);
//
//        Graph<Module> modules = resolveProjectOutput(projectModule);
//        GraphPrinter.printGraphAsTree(modules, projectModule, "");
//
//        processDependencies(modules, projectModule, module -> {
//            System.out.println("Processing module: " + module.getDebugName());
//        });
//
//        List<Path> compileClassPath = ModuleUtils.getCompileClassPath(projectModule);
//        System.out.println(compileClassPath);
//
//        Path moduleTestFile = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/lib/src/main/java/com/tyron/code/java/ModuleFileManager.java");
//        Completor completor = new Completor();
//        CompletionResult completionResult = completor.getCompletionResult(projectModule, moduleTestFile, 25, 20);
//        System.out.println(completionResult);
//    }
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
//    public MutableGraph<Module> resolveProjectOutput(ProjectModule module) {
//        MutableGraph<Module> graph = GraphBuilder.directed().build();
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
