package com.tyron.code.java.completion;

import com.tyron.code.java.analysis.Analyzer;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.impl.FileSystemModuleManager;
import com.tyron.code.project.impl.ModuleInitializer;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.JdkModuleImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseCompletionTest {

    protected FileManager fileManager;
    protected ModuleManager moduleManager;
    protected JavaModuleImpl rootModule;

    protected Completor completor;

    protected Analyzer analyzer;

    @BeforeAll
    public void setup() throws Exception {
        Path root = Files.createTempDirectory("test");
        fileManager = new SimpleFileManager(root, List.of());
        moduleManager = new FileSystemModuleManager(fileManager, root);
        moduleManager.initialize();

        rootModule = (JavaModuleImpl) moduleManager.getRootModule().getIncludedModules().get(0);

        JdkModuleImpl jdkModule = new JdkModuleImpl(moduleManager, Paths.get("src/test/resources/android.jar"), "11");
        new ModuleInitializer().initializeModule(jdkModule);
        rootModule.setJdk(jdkModule);

        analyzer = new Analyzer(fileManager, rootModule);
        completor = new Completor(fileManager, analyzer);
    }

    protected List<CompletionCandidate> complete(String contents) {
        assert contents.contains("@complete");

        int completeIndex = contents.indexOf("@complete");
        String before = contents.substring(0, completeIndex);
        String after = contents.substring(contents.indexOf("@complete") + "@complete".length());

        String modified = before + after;
        Path tempFile;
        try {
            tempFile = Files.createTempFile("", ".java");
            Files.writeString(tempFile, modified);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        fileManager.setSnapshotContent(tempFile.toUri(), modified);


        int[] offset = convertOffsetToLineColumn(modified, completeIndex);
        int line = offset[0];
        int column = offset[1];

        CompletionResult completionResult = completor.getCompletionResult(tempFile, line, column);
        return completionResult.getCompletionCandidates();
    }

    protected List<String> completeString(String contents) {
        return complete(contents).stream().map(CompletionCandidate::getName).toList();
    }

    private static int[] convertOffsetToLineColumn(String input, int offset) {
        if (offset < 0 || offset > input.length()) {
            throw new IllegalArgumentException("Invalid offset value");
        }

        int line = 0;
        int column = 0;

        for (int i = 0; i < offset; i++) {
            if (input.charAt(i) == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        }

        return new int[]{line, column};
    }
}
