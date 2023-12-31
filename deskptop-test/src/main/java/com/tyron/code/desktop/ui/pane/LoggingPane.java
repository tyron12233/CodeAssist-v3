package com.tyron.code.desktop.ui.pane;

import com.tyron.code.desktop.ui.control.richtext.Editor;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.LineContainer;
import com.tyron.code.desktop.ui.control.richtext.linegraphics.LineGraphicFactory;
import com.tyron.code.desktop.util.FxThreadUtils;
import com.tyron.code.logging.LogConsumer;
import com.tyron.code.logging.Logging;
import com.tyron.code.project.util.ThreadPoolFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LoggingPane extends BorderPane implements LogConsumer<String> {

    private final List<LogCallInfo> infos = new ArrayList<>();
    private final Editor editor = new Editor();
    private final CodeArea codeArea = editor.getCodeArea();


    public LoggingPane() {
        Logging.addLogConsumer(this);
        codeArea.setEditable(false);
        editor.getRootLineGraphicFactory().addLineGraphicFactory(new LoggingLineFactory());
        setCenter(editor);

        infos.add(new LogCallInfo("Initial", Level.TRACE, "", null));
        codeArea.appendText("Current log will write to: NONE");

        // We want to reduce the calls to the FX thread, so we will chunk log-appends into groups
        // occurring every 500ms, which shouldn't be too noticeable, and save us some CPU time.
        ThreadPoolFactory.newScheduledThreadPool("logging-pane")
                .scheduleAtFixedRate(() -> {
                    try {
                        int skip = codeArea.getParagraphs().size();
                        int size = infos.size();
                        if (size > skip) {
                            String collectedMessageLines = infos.stream().skip(skip)
                                    .map(LogCallInfo::getAndPruneContent)
                                    .collect(Collectors.joining("\n"));
                            FxThreadUtils.run(() -> {
                                codeArea.appendText("\n" + collectedMessageLines);
                                codeArea.showParagraphAtBottom(codeArea.getParagraphs().size() - 1);
                            });
                        }
                    } catch (Throwable t) {
                        // We don't want to cause infinite loops by causing uncaught exceptions to trigger another
                        // logger call, so we will just print the trace here and move on.
                        t.printStackTrace();
                    }
                }, 100, 500, TimeUnit.MILLISECONDS);

    }

    private static class LogCallInfo {
        private final String loggerName;
        private final Level level;
        private final Throwable throwable;
        private String messageContent;

        LogCallInfo(@NotNull String loggerName,
                    @NotNull Level level,
                    @NotNull String messageContent,
                    @Nullable Throwable throwable) {
            this.loggerName = loggerName;
            this.level = level;
            this.messageContent = messageContent;
            this.throwable = throwable;
        }

        /**
         * Gets the message content once, then clears it, so we don't hold a constant reference to it.
         *
         * @return Message content of log call.
         */
        @NotNull
        public String getAndPruneContent() {
            String content = messageContent;
            if (content == null)
                throw new PruneError();
            messageContent = null;
            return content;
        }

    }

    @Override
    public void accept(String loggerName, Level level, String messageContent) {
        infos.add(new LogCallInfo(loggerName, level, messageContent, null));
    }

    @Override
    public void accept(String loggerName, Level level, String messageContent, Throwable throwable) {
        infos.add(new LogCallInfo(loggerName, level, messageContent, throwable));
    }


    private class LoggingLineFactory implements LineGraphicFactory {
        private static final Insets PADDING = new Insets(0, 5, 0, 0);
        private static final double SIZE = 4;
        private static final double[] TRIANGLE = {
                SIZE / 2, 0,
                SIZE, SIZE,
                0, SIZE
        };

        @Override
        public int priority() {
            return -1;
        }

        @Override
        public void apply(@NotNull LineContainer container, int paragraph) {
            if (paragraph >= infos.size())
                return;
            Shape shape = getShape(paragraph);

            // Wrap and provide right-side padding to give the indicator space between it and the line no.
            HBox wrapper = new HBox(shape);
            wrapper.setAlignment(Pos.CENTER);
            wrapper.setPadding(PADDING);
            container.addHorizontal(wrapper);
        }

        @NotNull
        private Shape getShape(int paragraph) {
            LogCallInfo info = infos.get(paragraph);
            Shape shape = switch (info.level) {
                case ERROR -> info.throwable == null ?
                        new Circle(SIZE, Color.RED) : new Polygon(TRIANGLE);
                case WARN -> new Circle(SIZE, Color.YELLOW);
                case INFO -> new Circle(SIZE, Color.LIGHTBLUE);
                case DEBUG -> new Circle(SIZE, Color.CORNFLOWERBLUE);
                case TRACE -> new Circle(SIZE, Color.DODGERBLUE);
            };
            shape.setOpacity(0.65);
            return shape;
        }

        @Override
        public void install(@NotNull Editor editor) {
            // no-op
        }

        @Override
        public void uninstall(@NotNull Editor editor) {
            // no-op
        }
    }

    private static class PruneError extends RuntimeException {
        private PruneError() {
            super(null, null, false, false);
        }
    }
}
