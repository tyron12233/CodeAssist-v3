package com.tyron.code.java.analysis;

import com.tyron.code.java.ModuleFileManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.FileSnapshot;
import com.tyron.code.project.model.ProjectModule;
import shadow.com.sun.source.tree.CompilationUnitTree;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.api.JavacTool;
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

public class Analyzer {

    private static final JavacTool SYSTEM_PROVIDER = JavacTool.create();

    private final Object lock = new Object();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private volatile FutureTask<AnalysisResult> currentTask;

    private final Consumer<String> progressConsumer;
    private final ModuleFileManager moduleFileManager;

    public Analyzer(FileManager fileManager, ProjectModule projectModule, Consumer<String> progressConsumer) {
        this.progressConsumer = progressConsumer;
        moduleFileManager = new ModuleFileManager(fileManager, projectModule);
    }

    public synchronized void analyze(Path path, String contents, ProjectModule projectModule, Consumer<AnalysisResult> consumer) {
        analyzeInternal(projectModule, path, contents, consumer);
    }

    private void analyzeInternal(ProjectModule projectModule, Path path, String contents, Consumer<AnalysisResult> consumer) {
        if (currentTask != null) {
            cancelled.set(true);
            try {
                currentTask.cancel(true);
                currentTask.get(); // Wait for completion before starting new
            } catch (Exception e) {
                handleCancellationException(e);
            } finally {
                cancelled.set(false);
            }
        }

        currentTask = new FutureTask<>(new AnalyzeCallable(projectModule, path, contents, consumer));
        new Thread(currentTask).start();
    }

    private void handleCancellationException(Exception e) {
        if (e instanceof CancellationException || (e.getCause() instanceof CancellationException)) {
            throw new CancellationException();
        }
        throw new RuntimeException(e.getCause());
    }

    private JavacTaskImpl getJavacTask(Path path, String contents, Context context, ProjectModule projectModule) {
        return (JavacTaskImpl) SYSTEM_PROVIDER.getTask(
                new PrintWriter(Writer.nullWriter()),
                moduleFileManager,
                diagnostic -> {},
                List.of(
                        "-XDide",
                        "-XDcompilePolicy=byfile",
                        "-XD-Xprefer=source",
                        "-XDkeepCommentsOverride=ignore",
                        "-XDsuppressAbortOnBadClassFile",
                        "-XDshould-stop.at=GENERATE",
                        "-XDdiags.formatterOptions=-source",
                        "-XDdiags.layout=%L%m|%L%m|%L%m",
                        "-g:source",
                        "-g:lines",
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
        private final ProjectModule projectModule;
        private final Path file;
        private final String contents;
        private final Consumer<AnalysisResult> consumer;

        public AnalyzeCallable(ProjectModule projectModule, Path file, String contents, Consumer<AnalysisResult> consumer) {
            this.projectModule = projectModule;
            this.file = file;
            this.contents = contents;
            this.consumer = consumer;
        }

        @Override
        public AnalysisResult call() throws Exception {
            progressConsumer.accept("1. Getting task from task pool.");
            Context context = new Context();
            JavacTaskImpl javacTask = getJavacTask(file, contents, context, projectModule);

            moduleFileManager.setCompletingFile(file, contents);
            try {
                synchronized (lock) {
                    checkCancelled();
                    progressConsumer.accept("3. Parsing");

                    Iterable<? extends CompilationUnitTree> parsed = javacTask.parse();
                    checkCancelled();

                    progressConsumer.accept("4. Enter");
                    Iterable<? extends Element> elements = javacTask.enterTrees(parsed);
                    checkCancelled();

                    progressConsumer.accept("5. Attribute");

                    Iterable<? extends Element> analyzed = javacTask.analyze();

                    AnalysisResult analysisResult = new AnalysisResult(projectModule, javacTask, parsed.iterator().next(), analyzed, Analyzer.this);
                    consumer.accept(analysisResult);
                    return analysisResult;
                }
            } finally {
                moduleFileManager.setCompletingFile(null, null);
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