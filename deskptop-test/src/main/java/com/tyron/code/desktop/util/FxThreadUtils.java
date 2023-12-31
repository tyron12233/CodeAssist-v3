package com.tyron.code.desktop.util;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tyron.code.project.util.ThreadPoolFactory.newScheduledThreadPool;

public class FxThreadUtils {
    private static final ScheduledExecutorService scheduledService = newScheduledThreadPool("CodeAssist misc");

    public static void run(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void delayedRun(long delayMs, Runnable action) {
        CompletableFuture<?> future = new CompletableFuture<>();
        scheduledService.schedule(() -> {
            try {
                action.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}
