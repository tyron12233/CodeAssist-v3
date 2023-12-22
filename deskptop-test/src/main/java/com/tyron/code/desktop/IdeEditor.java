package com.tyron.code.desktop;

import com.tyron.code.project.file.FileManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class IdeEditor extends RSyntaxTextArea {

    public IdeEditor(FileManager fileManager, Path file) {

        Optional<CharSequence> fileContent = fileManager.getFileContent(file);
        CharSequence content = fileContent.orElseThrow();

        try {
            fileManager.openFileForSnapshot(file.toUri(), content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setText((String) content);
        setSyntaxEditingStyle(SYNTAX_STYLE_JAVA);
    }
}
