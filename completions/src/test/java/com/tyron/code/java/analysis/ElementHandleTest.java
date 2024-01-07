package com.tyron.code.java.analysis;

import com.google.common.truth.Truth;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.impl.FileSystemModuleManager;
import com.tyron.code.project.impl.ModuleInitializer;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.JdkModuleImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import shadow.javax.lang.model.element.Element;
import shadow.javax.lang.model.element.ElementKind;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElementHandleTest {

    protected FileManager fileManager;
    protected ModuleManager moduleManager;
    protected JavaModuleImpl rootModule;
    protected Analyzer analyzer;

    @BeforeAll
    void setup() throws IOException {
        Path root = Files.createTempDirectory("test");
        fileManager = new SimpleFileManager(root, List.of());
        moduleManager = new FileSystemModuleManager(fileManager, root);
        moduleManager.initialize();

        rootModule = (JavaModuleImpl) moduleManager.getRootModule().getIncludedModules().get(0);

        JdkModuleImpl jdkModule = new JdkModuleImpl(moduleManager, Paths.get("src/test/resources/android.jar"), "11");
        new ModuleInitializer().initializeModule(jdkModule);
        rootModule.setJdk(jdkModule);

        analyzer = new Analyzer(fileManager, rootModule);
    }


    @Test
    void test() throws IOException {
        Path file = Files.createTempFile("", ".java");
        String contents = """
                class Main {
                    static void main() {
                        Main.main();
                    }
                    
                    void instance();
                }
                """;
        Files.writeString(file, contents);

        CompletableFuture<ElementHandle<?>> handleCompletableFuture = new CompletableFuture<>();

        analyzer.analyze(file, contents, analysisResult -> {
            handleCompletableFuture.complete(
                    ElementHandle.create(analysisResult.analyzed().iterator().next())
            );
        });

        ElementHandle<?> handle = handleCompletableFuture.join();

        Truth.assertThat(handle).isNotNull();
        Truth.assertThat(handle.getKind()).isEqualTo(ElementKind.CLASS);
        Truth.assertThat(handle.getQualifiedName()).isEqualTo("Main");

        CompletableFuture<Element> second = new CompletableFuture<>();

        analyzer.analyze(file, contents, analysisResult -> {
            Element resolve = handle.resolve(analysisResult);
            second.complete(resolve);
        });

        Element join = second.join();
        Truth.assertThat(join).isNotNull();
        Truth.assertThat(join.getKind()).isEqualTo(ElementKind.CLASS);
        Truth.assertThat(join.getSimpleName()).isEqualTo("Main");
    }
}