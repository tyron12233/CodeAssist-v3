package com.tyron.code.java.analysis;

import com.tyron.code.java.ModuleFileManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.FileSnapshot;
import com.tyron.code.project.model.ProjectModule;
import shadow.com.sun.source.tree.CompilationUnitTree;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.api.JavacTool;
import shadow.com.sun.tools.javac.code.Symtab;
import shadow.com.sun.tools.javac.util.Context;
import shadow.javax.lang.model.element.Element;

import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class CustomContext extends Context {

    public <T> void drop(Class<T> c) {
        ht.remove(key(c));
    }

    @Override
    public <T> void put(Key<T> key, T data) {
        if (ht.containsKey(key)) {
            System.out.println("Duplicate key " + ht.get(key).getClass());
        }
        super.put(key, data);
    }

    @Override
    public <T> void put(Class<T> clazz, T data) {
        if (ht.containsKey(key(clazz))) {
            System.out.println(clazz);
        }
        super.put(clazz, data);
    }

    public void clear() {
        ht.clear();
    }
}

public class Analyzer {

    private static final JavacTool systemProvider = JavacTool.create();

    private final Object lock = new Object();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile FutureTask<AnalysisResult> currentTask;

    private final Consumer<String> progressConsumer;
    private final ModuleFileManager moduleFileManager;

    public Analyzer(FileManager fileManager, ProjectModule projectModule, Consumer<String> progressConsumer) {
        this.progressConsumer = progressConsumer;
        moduleFileManager = new ModuleFileManager(fileManager, projectModule);
    }


    public void analyze(Path path, String contents, ProjectModule projectModule, Consumer<AnalysisResult> consumer) {
        progressConsumer.accept("1. Getting task from task pool.");

        Context context = new Context();
        JavacTaskImpl javacTask = getJavacTask(path, contents, context, projectModule);


        try {
            moduleFileManager.setCompletingFile(path, contents);

            checkCancelled();
            progressConsumer.accept("2. Got task");
            AnalysisResult analysisResult = analyzeInternal(javacTask, projectModule, path, contents);
            checkCancelled();
            consumer.accept(analysisResult);
        } finally {
            moduleFileManager.clearCompletingFile();
        }


//        taskPool.getTask(
//                new PrintWriter(Writer.nullWriter()),
//                moduleFileManager,
//                diagnostic -> {
//                    // TODO: Diagnostics report
//                },
//                List.of(
//                        "-XDide",
//                        "-XDcompilePolicy=byfile",
//                        "-XD-Xprefer=source",
//                        "-XDkeepCommentsOverride=ignore",
//                        "-XDsuppressAbortOnBadClassFile",
//                        "-XDshould-stop.at=GENERATE",
//                        "-XDdiags.formatterOptions=-source",
//                        "-XDdiags.layout=%L%m|%L%m|%L%m",
//                        "-XDbreakDocCommentParsingOnError=false",
//                        "-Xlint:cast",
//                        "-Xlint:deprecation",
//                        "-Xlint:empty",
//                        "-Xlint:fallthrough",
//                        "-Xlint:finally",
//                        "-Xlint:path",
//                        "-Xlint:unchecked",
//                        "-Xlint:varargs",
//                        "-Xlint:static"
//                        ),
//                null,
//                List.of(FileSnapshot.create(path.toUri(), contents)),
//                javacTask -> {
//                    try {
//                        moduleFileManager.setCompletingFile(path, contents);
//
//                        checkCancelled();
//                        progressConsumer.accept("2. Got task");
//                        AnalysisResult analysisResult = analyzeInternal(((JavacTaskImpl) javacTask), projectModule, path, contents);
//                        checkCancelled();
//                        consumer.accept(analysisResult);
//                    } finally {
//                        moduleFileManager.clearCompletingFile();
//                    }
//                    return null;
//                }
//        );
    }

    private AnalysisResult analyzeInternal(JavacTaskImpl javacTask, ProjectModule projectModule, Path path, String contents) {

        if (currentTask != null) {
            cancelled.set(true);
            try {
                currentTask.get(); // Wait for completion before starting new
            } catch (Exception e) {
                if (e instanceof CancellationException cancellationException) {
                    throw cancellationException;
                }

                throw new RuntimeException(e.getCause());
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
            if (e instanceof CancellationException) {
                throw ((CancellationException) e);
            }
            throw new RuntimeException(e);
        }
    }

    private JavacTaskImpl getJavacTask(Path path, String contents, Context context, ProjectModule projectModule) {
        return (JavacTaskImpl) systemProvider.getTask(
                new PrintWriter(Writer.nullWriter()),
                moduleFileManager,
                diagnostic -> {
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
                        "-g:source", // NOI18N, Make the compiler to maintain source file info
                        "-g:lines", // NOI18N, Make the compiler to maintain line table
                        "-g:vars",
                        "-bootclasspath",
                        projectModule.getJdkModule().getJarPath().toString(),
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
                List.of(FileSnapshot.create(path.toUri(), contents)),
                context
        );
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
            synchronized (lock) {
                checkCancelled();
                progressConsumer.accept("3. Parsing");

                Iterable<? extends CompilationUnitTree> parsed = javacTask.parse();
                checkCancelled();

                progressConsumer.accept("4. Enter");
                // this phase initializes, the table but does not resolve references yet
                Iterable<? extends Element> elements = javacTask.enterTrees(parsed);

                checkCancelled();


                progressConsumer.accept("5. Attribute");
                // the analyze() method performs other several tasks that we don't need during
                // completion such as type checking which we don't need.
                // we just want to attribute (resolve references) `
//                var attributedElements = javacTask.getTodo().stream()
//                        .peek(it -> javacTask.attributeTree(it.tree, it))
//                        .map(it -> it.tree)
//                        .map(tree -> switch (tree.getTag()) {
//                            case CLASSDEF -> ((JCTree.JCClassDecl) tree).sym;
//                            case MODULEDEF -> ((JCTree.JCModuleDecl) tree).sym;
//                            case PACKAGEDEF -> ((JCTree.JCPackageDecl) tree).packge;
//                            default -> null;
//                        }).filter(Objects::nonNull)
//                        .map(Element.class::cast)
//                        .toList();

                Iterable<? extends Element> analyzed = javacTask.analyze();

                return new AnalysisResult(projectModule, javacTask, parsed.iterator().next(), analyzed, Analyzer.this);
            }
        }
    }

    public void cancel() {
        cancelled.set(true);
    }

    public void checkCancelled() {
        if (cancelled.get() || Thread.interrupted()) {
            throw new CancellationException();
        }
    }
}
