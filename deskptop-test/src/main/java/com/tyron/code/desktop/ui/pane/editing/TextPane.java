package com.tyron.code.desktop.ui.pane.editing;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.bracket.BracketMatchGraphicFactory;
import com.tyron.code.desktop.ui.control.richtext.bracket.SelectedBracketTracking;
import com.tyron.code.desktop.ui.control.richtext.problem.ProblemGraphicFactory;
import javafx.scene.layout.BorderPane;

import java.util.concurrent.atomic.AtomicBoolean;

public class TextPane extends BorderPane  {

    protected final AtomicBoolean updateLock = new AtomicBoolean();
    protected final Editor editor;

    public TextPane() {

        // Configure the editor
        editor = new Editor();
        editor.setSelectedBracketTracking(new SelectedBracketTracking());
        editor.getRootLineGraphicFactory().addLineGraphicFactories(
                new BracketMatchGraphicFactory(),
                new ProblemGraphicFactory()
        );

        setCenter(editor);
    }
}
