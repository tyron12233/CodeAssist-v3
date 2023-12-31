package com.tyron.code.desktop;

import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.java.analysis.Analyzer;
import com.tyron.code.java.completion.CompletionCandidate;
import com.tyron.code.java.completion.CompletionResult;
import com.tyron.code.java.completion.Completor;
import com.tyron.code.java.completion.ElementCompletionCandidate;
import com.tyron.code.project.ModuleManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.impl.FileSystemModuleManager;
import com.tyron.code.project.impl.ModuleInitializer;
import com.tyron.code.project.impl.model.JarModuleImpl;
import com.tyron.code.project.impl.model.JavaModuleImpl;
import com.tyron.code.project.impl.model.JdkModuleImpl;
import com.tyron.code.project.model.module.JarModule;
import com.tyron.code.project.model.module.JdkModule;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

public class TestEditor extends JFrame {

    private final Completor completor;
    private JavaModuleImpl javaModule;
    private ModuleManager fileSystemModuleManager;

    private Path editingFile;

    RSyntaxTextArea textArea;
    private final SimpleFileManager simpleFileManager = new SimpleFileManager();

    public TestEditor() {
        setupTestProject();

        Analyzer analyzer = new Analyzer(simpleFileManager, javaModule, System.out::println);
        completor = new Completor(simpleFileManager, analyzer);


        JPanel contentPane = new JPanel(new BorderLayout());
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.addParser(new AbstractParser() {
            @Override
            public ParseResult parse(RSyntaxDocument doc, String style) {

                DefaultParseResult parseResult = new DefaultParseResult(this);
                try {
                    analyzer.analyze(editingFile, doc.getText(0, doc.getLength()), result -> {
                        System.out.println("Running background analysis");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // ignored
                        }
                        System.out.println("Done background analysis");
                    });
                } catch (BadLocationException | CancellationException e) {
                    // ignored
                }
                return parseResult;
            }
        });
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException ioe) { // Never happens
            throw new RuntimeException(ioe);
        }
        contentPane.add(new RTextScrollPane(textArea));

        CompletionProvider provider = createCompletionProvider();


        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(textArea);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoCompleteSingleChoices(false);
        ac.setListCellRenderer(new CompletionCellRenderer());

        setContentPane(contentPane);
        setTitle("Test.java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        SourceClassInfo javaFileInfo = javaModule.getFiles().get(0);
        textArea.setText(simpleFileManager.getFileContent(editingFile).orElseThrow().toString());

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                simpleFileManager.setSnapshotContent(editingFile.toUri(), textArea.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                simpleFileManager.setSnapshotContent(editingFile.toUri(), textArea.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                simpleFileManager.setSnapshotContent(editingFile.toUri(), textArea.getText());
            }
        });
    }

    private void setupTestProject() {
        try {
            Path tempDirectory = Files.createTempDirectory("");
            editingFile = tempDirectory.resolve("Test.java");
            Files.createFile(editingFile);

            fileSystemModuleManager = new FileSystemModuleManager(simpleFileManager, tempDirectory);
            fileSystemModuleManager.initialize();

            javaModule = (JavaModuleImpl) fileSystemModuleManager.getRootModule();



            Files.writeString(editingFile, "package test;\n\npublic class Main {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}");
            Files.writeString(tempDirectory.resolve("Another.java"), "package test;\n public class Another { public void test() { System.out.println(); }}");
            fileSystemModuleManager.addOrUpdateFile(editingFile);
            fileSystemModuleManager.addOrUpdateFile(tempDirectory.resolve("Another.java"));

            simpleFileManager.openFileForSnapshot(editingFile.toUri(), "package test;\n\nclass Main {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}");

            Path androidJar = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/deskptop-test/android.jar");
            JdkModule jdkModule = new JdkModuleImpl(androidJar, "11");
            new ModuleInitializer().initializeModule(jdkModule);

            Path commonsJar = Paths.get("/home/tyronscott/Downloads/guava-33.0.0-jre.jar");

            JarModule commonsJarModule = new JarModuleImpl(commonsJar);
            new ModuleInitializer().initializeModule(commonsJarModule);

            javaModule.setJdk(jdkModule);
            javaModule.addImplementationDependency(commonsJarModule);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CompletionProvider createCompletionProvider() {
        return new TestProvider();
    }

    class TestProvider extends AbstractCompletionProvider {

        private final Segment seg = new Segment();

        @Override
        public String getAlreadyEnteredText(JTextComponent comp) {
            Document doc = comp.getDocument();

            int dot = comp.getCaretPosition();
            Element root = doc.getDefaultRootElement();
            int index = root.getElementIndex(dot);
            Element elem = root.getElement(index);
            int start = elem.getStartOffset();
            int len = dot - start;
            try {
                doc.getText(start, len, seg);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
                return EMPTY_STRING;
            }

            int segEnd = seg.offset + len;
            start = segEnd - 1;
            while (start >= seg.offset && isValidChar(seg.array[start])) {
                start--;
            }
            start++;

            len = segEnd - start;
            return len == 0 ? EMPTY_STRING : new String(seg.array, start, len);
        }

        private boolean isValidChar(char ch) {
            return Character.isLetterOrDigit(ch) || ch == '_';
        }

        @Override
        public List<Completion> getCompletionsAt(JTextComponent comp, Point p) {
            System.out.println();
            return Collections.emptyList();
        }

        @Override
        public List<Completion> getCompletions(JTextComponent comp) {
            int caretPosition = comp.getCaretPosition();
            int line;
            int column;
            try {
                line = textArea.getLineOfOffset(caretPosition);
                column = caretPosition - textArea.getLineStartOffset(line);

            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }


            CompletionResult completionResult = completor.getCompletionResult(editingFile, line, column);

            List<Completion> list = completionResult.getCompletionCandidates().stream()
                    .map(this::getCompletion)
                    .toList();
            System.out.println(list);
            return list;
        }

        private Completion getCompletion(CompletionCandidate candidate) {
            if (!(candidate instanceof ElementCompletionCandidate elementCompletionCandidate)) {
                return new BasicCompletion(this, candidate.getName());
            }

            CompletionCandidate.Kind kind = elementCompletionCandidate.getKind();
            if (kind != CompletionCandidate.Kind.METHOD) {
                return new BasicCompletion(this, candidate.getName());
            }

            return new FunctionCompletion(
                    this,
                    elementCompletionCandidate.getName(),
                    elementCompletionCandidate.getDetail().orElse("")
            );
        }

        @Override
        public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
            return Collections.emptyList();
        }
    }
}
