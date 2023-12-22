package com.tyron.code.desktop;

import com.tyron.code.java.completion.CompletionResult;
import com.tyron.code.java.completion.Completor;
import com.tyron.code.project.FileSystemModuleManager;
import com.tyron.code.project.file.SimpleFileManager;
import com.tyron.code.project.model.JarModule;
import com.tyron.code.project.model.ProjectModule;
import com.tyron.code.project.model.UnparsedJavaFile;
import com.tyron.code.project.util.JarReader;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
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

public class TestEditor extends JFrame {

    private final Completor completor;
    private ProjectModule projectModule;
    private FileSystemModuleManager fileSystemModuleManager;

    private Path editingFile;

    RSyntaxTextArea textArea;
    private final SimpleFileManager simpleFileManager = new SimpleFileManager();

    public TestEditor() {
        setupTestProject();

        completor = new Completor(simpleFileManager);

        JPanel contentPane = new JPanel(new BorderLayout());
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        contentPane.add(new RTextScrollPane(textArea));

        CompletionProvider provider = createCompletionProvider();

        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(textArea);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);

        setContentPane(contentPane);
        setTitle("Test.java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        UnparsedJavaFile unparsedJavaFile = projectModule.getFiles().get(0);
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

            projectModule = fileSystemModuleManager.getProjectModule();

            Files.writeString(editingFile, "package test;\n public class Main {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}");
            Files.writeString(tempDirectory.resolve("Another.java"), "package test;\n public class Another { public void test() { System.out.println(); }}");
            fileSystemModuleManager.addOrUpdateFile(editingFile);
            fileSystemModuleManager.addOrUpdateFile(tempDirectory.resolve("Another.java"));

            simpleFileManager.openFileForSnapshot(editingFile.toUri(), "package test; class Main {\n\tpublic static void main(String[] args) {\n\t\t\n\t}\n}");

            Path androidJar = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/deskptop-test/android.jar");
            JarModule jdkModule = JarModule.createJdkDependency(androidJar);
            List<JarReader.ClassInfo> infos = JarReader.readJarFile(androidJar.toString());
            infos.stream().map(it -> new UnparsedJavaFile(jdkModule, androidJar, it.getClassName(), it.getPackageQualifiers())).forEach(jdkModule::addClass);

            projectModule.addJdkDependency(jdkModule);

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
            Cursor cursor = comp.getCursor();

            int caretPosition = comp.getCaretPosition();
            int line;
            int column;
            try {
                line = textArea.getLineOfOffset(caretPosition);
                column = caretPosition - textArea.getLineStartOffset(line);

            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }


            CompletionResult completionResult = completor.getCompletionResult(projectModule, editingFile, line, column);

            return completionResult.getCompletionCandidates().stream()
                    .map(candidate -> {
                        return (Completion) new BasicCompletion(this, candidate.getName());
                    })
                    .toList();
        }

        @Override
        public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
            return Collections.emptyList();
        }
    }
}
