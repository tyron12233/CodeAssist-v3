package com.tyron.code.java.analysis;

import com.tyron.code.java.ModuleFileManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.ProjectModule;
import shadow.com.sun.source.tree.CompilationUnitTree;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.api.JavacTaskPool;
import shadow.com.sun.tools.javac.code.Type;
import shadow.com.sun.tools.javac.comp.AttrContext;
import shadow.com.sun.tools.javac.comp.Env;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.com.sun.tools.javac.util.Log;
import shadow.javax.lang.model.element.Element;
import shadow.javax.tools.JavaFileObject;
import shadow.javax.tools.SimpleJavaFileObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Analyzer {

    private final ConcurrentLinkedQueue<AnalysisResult> results = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile FutureTask<AnalysisResult> currentTask;

    private final JavacTaskPool taskPool = new JavacTaskPool(4);

    public Analyzer() {

    }


    public void analyze(Path path, String contents, FileManager fileManager, ProjectModule projectModule, Consumer<AnalysisResult> consumer) {
        taskPool.getTask(
                new PrintWriter(Writer.nullWriter()),
                new ModuleFileManager(fileManager, projectModule),
                diagnostic -> {
                    // TODO: Diagnostics report
                },
                List.of(
                        "-XDide",
                        "-XDcompilePolicy=byfile",
                        "-XD-Xprefer=source",
                        "-XDkeepCommentsOverride=ignore",
                        "-XDsuppressAbortOnBadClassFile",
                        "-XDshould-stop.at=GENERATE",
                        "-XDdiags.formatterOptions=-source",
                        "-XDdiags.layout=%L%m|%L%m|%L%m",
                        "-XDbreakDocCommentParsingOnError=false",
                        "-Xlint:cast",
                        "-Xlint:deprecation",
                        "-Xlint:empty",
                        "-Xlint:fallthrough",
                        "-Xlint:finally",
                        "-Xlint:path",
                        "-Xlint:unchecked",
                        "-Xlint:varargs",
                        "-Xlint:static"
                        ),
                null,
                List.of(),
                javacTask -> {
                    AnalysisResult analysisResult = analyzeInternal(((JavacTaskImpl) javacTask), projectModule, path, contents);
                    consumer.accept(analysisResult);
                    return null;
                }
        );
    }

    private AnalysisResult analyzeInternal(JavacTaskImpl javacTask, ProjectModule projectModule, Path path, String contents) {

        if (currentTask != null) {
            cancelled.set(true);
            try {
                currentTask.get(); // Wait for completion before starting new
            } catch (Exception e) {
                // ignored
            } finally {
                cancelled.set(false);
            }
        }


        currentTask = new FutureTask<>(new AnalyzeCallable(javacTask, projectModule, path, contents));

        new Thread(currentTask).start();

        try {
            return currentTask.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class AnalyzeCallable implements Callable<AnalysisResult> {
        private final JavacTaskImpl javacTask;
        private final ProjectModule projectModule;
        private final Path file;
        private final String contents;

        public AnalyzeCallable(JavacTaskImpl javacTask, ProjectModule projectModule, Path file, String contents) {
            this.javacTask = javacTask;
            this.projectModule = projectModule;
            this.file = file;
            this.contents = contents;
        }

        @Override
        public AnalysisResult call() throws Exception {
            synchronized (results) {
                Iterable<? extends CompilationUnitTree> parsed = javacTask.parse(new SimpleJavaFileObject(file.toUri(), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return contents;
                    }
                });

                // this phase initializes, the table but does not resolve references yet
                javacTask.enterTrees(parsed);

                // the analyze() method performs other several tasks that we don't need during
                // completion such as type checking which we don't need.
                // we just want to attribute (resolve references) `
                var attributedElements = javacTask.getTodo().stream()
                        .peek(it -> javacTask.attributeTree(it.tree, it))
                        .map(it -> it.tree)
                        .map(tree -> switch (tree.getTag()) {
                            case CLASSDEF -> ((JCTree.JCClassDecl) tree).sym;
                            case MODULEDEF -> ((JCTree.JCModuleDecl) tree).sym;
                            case PACKAGEDEF -> ((JCTree.JCPackageDecl) tree).packge;
                            default -> null;
                        }).filter(Objects::nonNull)
                        .map(Element.class::cast)
                        .toList();

                results.add(new AnalysisResult(javacTask, parsed.iterator().next(), attributedElements));
            }

            return results.peek();
        }
    }

    public void cancel() {
        cancelled.set(true);
    }
}
