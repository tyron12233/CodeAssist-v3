package com.tyron.code.java.completion;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class PackageCompletionTests extends BaseCompletionTest{

    @Test
    public void testPackageCompletionInsideMethodInvocations() {
        List<String> completed = complete("""
                import java.util.*;
                                
                class Main {
                    void main() {
                        new ArrayList<String>().stream()
                        .map(it -> it.toUpperCase())
                        .collect(java.util.stream.@complete
                    }
                }
                """);
        assertThat(completed).contains("Collectors");
    }

    @Test
    public void testPackageCompletionWorksNormally() {
        List<String> completed = complete("""
                class Main {
                    void main() {
                        java.util.@complete
                    }
                }
                """);
        assertThat(completed).isNotEmpty();
    }

    @Test
    public void testPackageCompletionWorksOnImports() {
        List<String> complete = complete("""
                import java.util.@complete
                """);
        assertThat(complete).contains("List");
    }

    @Test
    public void testPackageCompletionWorksOnStaticImports() {
        List<String> complete = complete("""
                import static java.lang.@complete
                """);
        assertThat(complete).isNotEmpty();
    }
}
