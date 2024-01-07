package com.tyron.code.java.completion;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CompletionVisibilityTest extends BaseCompletionTest {

    @Test
    void testStaticCallsShouldNotContainInstanceMethods() {
        List<String> completed = completeString("""
                class Main {
                    static void main() {
                        Main.@complete
                    }
                    
                    void instance();
                }
                """);

        Truth.assertThat(completed).doesNotContain("instance");
    }
}
