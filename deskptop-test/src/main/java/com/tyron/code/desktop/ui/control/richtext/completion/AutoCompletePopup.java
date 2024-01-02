package com.tyron.code.desktop.ui.control.richtext.completion;

import com.tyron.code.desktop.ui.control.IconView;
import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.EditorComponent;
import com.tyron.code.desktop.ui.control.richtext.source.CompletionProvider;
import com.tyron.code.desktop.util.FxThreadUtils;
import com.tyron.code.desktop.util.Icons;
import com.tyron.code.java.completion.CompletionCandidate;
import com.tyron.code.java.completion.CompletionResult;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Callback;
import org.fxmisc.richtext.model.PlainTextChange;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.tyron.code.desktop.ui.control.richtext.Editor.SHORT_DELAY_MS;

public class AutoCompletePopup extends Popup implements EditorComponent, Consumer<List<PlainTextChange>> {

    private final VBox itemsBox = new VBox();
    private final ObservableList<CompletionCandidate> items = FXCollections.observableList(new ArrayList<>());
    private final Label resultsCount = new Label("Results: 0");
    private final Label selectedLabel = new Label("Selected: 1/1");

    private final ArrayList<String> selectionQueue = new ArrayList<>();
    private final CompletionProvider provider;
    private final ListView<CompletionCandidate> itemsList;

    //    private final ArrayList<IdeSpecialParser.PossiblePiecePackage> factoryOrder = new ArrayList<>();
    private int selectionIndex = 0;

    private CompletableFuture<CompletionResult> previousCompletionRequest;

    private final EventHandler<MouseEvent> popupItemEvent = mouseEvent -> {
        if (mouseEvent.getSource() instanceof Node) {
            mouseEvent.consume();
            if (selectionIndex == itemsBox.getChildren().indexOf((Node) mouseEvent.getSource())) {
                this.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
            } else {
                Node selected = itemsBox.getChildren().get(selectionIndex);
                if (selected != null) {
                    int indexOf = itemsBox.getChildren().indexOf((Node) mouseEvent.getSource());
                    selected.getStyleClass().remove("selected-syntax");
                    itemsBox.getChildren().get(indexOf).getStyleClass().add("selected-syntax");
                    selectionIndex = indexOf;
                }
            }
        }
    };
    private Editor editor;

    public AutoCompletePopup(CompletionProvider provider) {
        this.provider = provider;
        BorderPane bottomPane = new BorderPane();
        itemsList = new ListView<>(items);
        VBox topBox = new VBox(itemsList, bottomPane);
//        topBox.getStyleClass().add("auto-complete-parent");
//        bottomPane.getStyleClass().add("ac-bottom");
//        resultsCount.getStyleClass().add("ac-results");
//        selectedLabel.getStyleClass().add("ac-out-of");
//        itemsBox.setMaxHeight(300);

        getContent().add(topBox);
        setAutoHide(true);

        topBox.setPrefWidth(420);
        itemsBox.setFillWidth(true);
        itemsBox.setBackground(Background.fill(Color.RED));
        itemsList.setFixedCellSize(30);
        itemsList.setMaxHeight(300);


//        topBox.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, SHADOW_COLOR, 20, 0.3, 0, 2));

        bottomPane.setLeft(resultsCount);
        bottomPane.setRight(selectedLabel);

        itemsBox.getChildren().addListener((ListChangeListener<Node>) change -> selectedLabel.setText((selectionIndex + 1) + "/" + change.getList().size()));

        itemsList.setCellFactory(param -> new CompletionListCell());
    }


    @Override
    public void install(@NotNull Editor editor) {
        editor.getTextChangeEventStream()
                .reduceSuccessions(Collections::singletonList, (objects, plainTextChange) ->
                        Stream.concat(objects.stream(), Stream.of(plainTextChange)).toList(), Duration.ofMillis(SHORT_DELAY_MS)
                )
                .addObserver(this);

        this.editor = editor;
    }

    @Override
    public void uninstall(@NotNull Editor editor) {
        this.editor = null;
    }

    @Override
    public void accept(List<PlainTextChange> textChange) {
        boolean shouldRequestCompletion = textChange.stream().anyMatch(this::shouldRequestCompletion);

        if (shouldRequestCompletion) {
            requestCompletion();
        }
    }

    private void requestCompletion() {
        if (previousCompletionRequest != null) {
            try {
                previousCompletionRequest.cancel(true);
                previousCompletionRequest.join();
            } catch (CancellationException ignored) {

            }
        }

        previousCompletionRequest = CompletableFuture.supplyAsync(() -> provider.getCompletionSuggestions(editor));
        previousCompletionRequest.thenAccept(result -> {
            List<CompletionCandidate> completions = result.getCompletionCandidates();
            if (completions.isEmpty()) {
                return;
            }
            FxThreadUtils.run(() -> {
                items.clear();
                items.addAll(completions);

                itemsList.setPrefHeight(items.size() * itemsList.getFixedCellSize());


                resultsCount.setText("Results: " + completions.size());

                Bounds caretBounds = editor.getCodeArea().getCaretBounds().orElseThrow();
                show(editor, caretBounds.getMaxX(), caretBounds.getMaxY());
            });
        });
    }

    private boolean shouldRequestCompletion(PlainTextChange textChange) {
        if (textChange.getInserted().length() == 1) {
            char c = textChange.getInserted().charAt(0);
            return Character.isLetterOrDigit(c) || c == '_' || c == '.';
        }
        return false;
    }
}
