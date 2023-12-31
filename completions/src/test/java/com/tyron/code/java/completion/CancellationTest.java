package com.tyron.code.java.completion;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CancellationTest extends BaseCompletionTest {

    @Test
    public void testOldAnalysisShouldBeCancelled() {
        assertThrows(CancellationException.class, this::actualTest);
    }

    private void actualTest() throws InterruptedException {
        final CancellationException[] cancellationException = {null};
        Thread thread = new Thread(() -> {
            try {
                analyzer.analyze(Paths.get("test"), "", result -> {
                    try {
                        System.out.println("Running 1");
                        sleep(500);
                    } catch (CancellationException e) {
                        cancellationException[0] = e;
                    }
                });
            } catch (CancellationException e) {
                cancellationException[0] = e;
            }
        }, "First Thread");
        thread.start();

//        sleep(2);
        analyzer.analyze(Paths.get("test"), "", analysisResult -> {
            System.out.println("Running 2");
        });
        thread.join();

        if (cancellationException[0] != null) {
            throw cancellationException[0];
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
