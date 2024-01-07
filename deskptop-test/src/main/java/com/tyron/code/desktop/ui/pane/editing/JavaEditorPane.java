package com.tyron.code.desktop.ui.pane.editing;

import com.tyron.code.desktop.services.navigation.Navigable;
import com.tyron.code.desktop.services.navigation.UpdatableNavigable;
import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.bracket.BracketMatchGraphicFactory;
import com.tyron.code.desktop.ui.control.richtext.bracket.SelectedBracketTracking;
import com.tyron.code.desktop.ui.control.richtext.completion.AutoCompletePopup;
import com.tyron.code.desktop.ui.control.richtext.problem.*;
import com.tyron.code.desktop.ui.control.richtext.source.CompletionProvider;
import com.tyron.code.desktop.util.WorkspaceUtil;
import com.tyron.code.diagnostic.Diagnostic;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.java.analysis.Analyzer;
import com.tyron.code.java.completion.CompletionCandidate;
import com.tyron.code.java.completion.CompletionResult;
import com.tyron.code.java.completion.Completor;
import com.tyron.code.path.PathNode;
import com.tyron.code.path.impl.SourceClassPathNode;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.util.Unchecked;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.model.TwoDimensional;
import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavaEditorPane extends BorderPane implements UpdatableNavigable {

    private final JavaModule javaModule;
    private Completor completor;
    protected final AtomicBoolean updateLock = new AtomicBoolean();
    protected final Editor editor;
    protected SourceClassPathNode pathNode;
    private Analyzer analyzer;

    public JavaEditorPane(JavaModule javaModule) {
        this.javaModule = javaModule;
        // Configure the editor
        editor = new Editor();
        editor.setSelectedBracketTracking(new SelectedBracketTracking());
        editor.getRootLineGraphicFactory().addLineGraphicFactories(
                new BracketMatchGraphicFactory(),
                new ProblemGraphicFactory()
        );
        editor.setProblemTracking(new ProblemTracking());

        setCenter(editor);

        CompletionProvider completionProvider = editor -> {
            if (completor == null) {
                return CompletionResult.builder().build();
            }

            int offset = editor.getCodeArea().getCaretPosition();
            TwoDimensional.Position position = editor.getCodeArea().offsetToPosition(offset, TwoDimensional.Bias.Backward);

            CompletionResult completionResult = completor.getCompletionResult(pathNode.getValue().getPath().toAbsolutePath(), position.getMajor(), position.getMinor());

            List<Diagnostic> diagnostics = analyzer.getDiagnostics();
            return completionResult;
        };
        AutoCompletePopup popup = new AutoCompletePopup(completionProvider);
        popup.install(editor);
    }

    @NotNull
    @Override
    public PathNode<?> getPath() {
        return pathNode;
    }

    @NotNull
    @Override
    public Collection<Navigable> getNavigableChildren() {
        return Collections.emptyList();
    }

    @Override
    public void disable() {

    }

    @Override
    public void onUpdatePath(@NotNull PathNode<?> path) {
        for (Navigable navigableChild : getNavigableChildren()) {
            if (navigableChild instanceof UpdatableNavigable updatableNavigable) {
                updatableNavigable.onUpdatePath(path);
            }
        }

        if (!updateLock.get() && path instanceof SourceClassPathNode sourceClassPathNode) {
            updateLock.set(true);
            this.pathNode = sourceClassPathNode;
            SourceClassInfo classInfo = sourceClassPathNode.getValue();
            Workspace workspace = KoinJavaComponent.get(Workspace.class);


            FileManager fileManager = WorkspaceUtil.getScoped(workspace, FileManager.class);
            CharSequence contents = fileManager.getFileContent(classInfo.getPath().toAbsolutePath()).orElseThrow();
            Unchecked.runnable(() -> fileManager.openFileForSnapshot(classInfo.getPath().toUri(), contents.toString())).run();
            editor.setText(contents.toString());

            editor.getTextChangeEventStream()
                    .addObserver(plainTextChange -> fileManager.setSnapshotContent(classInfo.getPath().toUri(), editor.getText()));

            analyzer = new Analyzer(fileManager, javaModule);
            completor = new Completor(fileManager, analyzer);
            updateLock.set(false);
        }
    }
}
