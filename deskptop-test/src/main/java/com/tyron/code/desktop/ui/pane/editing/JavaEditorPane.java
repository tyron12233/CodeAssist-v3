package com.tyron.code.desktop.ui.pane.editing;

import com.tyron.code.desktop.services.navigation.Navigable;
import com.tyron.code.desktop.services.navigation.UpdatableNavigable;
import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.bracket.BracketMatchGraphicFactory;
import com.tyron.code.desktop.ui.control.richtext.bracket.SelectedBracketTracking;
import com.tyron.code.desktop.ui.control.richtext.problem.ProblemGraphicFactory;
import com.tyron.code.info.SourceClassInfo;
import com.tyron.code.path.PathNode;
import com.tyron.code.path.impl.SourceClassPathNode;
import com.tyron.code.project.Workspace;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.util.Unchecked;
import javafx.scene.layout.BorderPane;
import kotlin.jvm.JvmClassMappingKt;
import org.jetbrains.annotations.NotNull;
import org.koin.core.component.KoinScopeComponent;
import org.koin.core.component.KoinScopeComponentKt;
import org.koin.core.scope.Scope;
import org.koin.java.KoinJavaComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavaEditorPane extends BorderPane implements UpdatableNavigable {

    protected final AtomicBoolean updateLock = new AtomicBoolean();
    protected final Editor editor;
    protected SourceClassPathNode pathNode;

    public JavaEditorPane() {

        // Configure the editor
        editor = new Editor();
        editor.setSelectedBracketTracking(new SelectedBracketTracking());
        editor.getRootLineGraphicFactory().addLineGraphicFactories(
                new BracketMatchGraphicFactory(),
                new ProblemGraphicFactory()
        );

        setCenter(editor);
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
            Scope scope = KoinJavaComponent.getKoin().getScope(KoinScopeComponentKt.getScopeId(workspace));
            FileManager fileManager = scope.get(JvmClassMappingKt.getKotlinClass(FileManager.class), null, null);
            CharSequence contents = fileManager.getFileContent(classInfo.getPath()).orElseThrow();
            Unchecked.runnable(() -> fileManager.openFileForSnapshot(classInfo.getPath().toUri(), contents.toString()));
            editor.setText(contents.toString());
            updateLock.set(false);
        }
    }
}
