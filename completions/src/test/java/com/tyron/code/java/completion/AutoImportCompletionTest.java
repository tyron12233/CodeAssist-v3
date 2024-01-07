package com.tyron.code.java.completion;

import com.google.common.truth.Truth;
import com.tyron.code.java.model.ResolveAction;
import com.tyron.code.java.model.ResolveActionParams;
import com.tyron.code.java.model.ResolveAddImportTextEditsParams;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AutoImportCompletionTest extends BaseCompletionTest {

    @Test
    public void testAutoImportCompletion() {
        String text = """
                class Main {
                    public static void main(String[] args) {
                        List.o@complete
                    }
                }
                """;
        List<CompletionCandidate> complete = complete(text);
        for (CompletionCandidate completionCandidate : complete) {
            Truth.assertThat(completionCandidate.getResolveActions()).isNotEmpty();
            Truth.assertThat(completionCandidate.getResolveActions()).containsKey(ResolveAction.ADD_IMPORT_TEXT_EDIT);

            ResolveActionParams resolveActionParams = completionCandidate.getResolveActions().get(ResolveAction.ADD_IMPORT_TEXT_EDIT);
            Truth.assertThat(resolveActionParams).isInstanceOf(ResolveAddImportTextEditsParams.class);
            Truth.assertThat(((ResolveAddImportTextEditsParams) resolveActionParams).classFullName).isEqualTo("java.util.List");
        }
    }

}
