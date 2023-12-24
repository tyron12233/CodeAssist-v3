# Completion Module

This module contains all the necessary logic for providing java completions
in CodeAssist.

## Implementation Details
(Not yet written)

# Testing
This repository contains a code completion testing framework for Java using JUnit 5. The base completion class BaseCompletionTest provides a foundation for testing code completions in Java code snippets.

1. **BaseCompletionTest Class:**
    - The **BaseCompletionTest** class serves as the foundation for code completion tests.
    - It sets up the testing environment, initializes required components, and provides a method for code completion.
2. **Creating Test Classes:**
    - Extend the **BaseCompletionTest** class to create test classes for specific completion scenarios.
    - Use the complete method to obtain completion suggestions for a given code snippet.
3. **Example Test:**
    - See the example test class PackageCompletionTests for reference.
    - Tests demonstrate completion scenarios inside method invocations, normal code blocks, imports, and static imports.
   ```java
    public class PackageCompletionTests extends BaseCompletionTest {
   
        @Test
        public void testPackageCompletionWorksOnStaticImports() {
            List<String> complete = complete("""
                    import static java.lang.@complete
                    """);
            assertThat(complete).isNotEmpty();
        }
    }
   ```
   
4. **Writing Test Methods**
   - Write test methods that contain Java code snippets with placeholders for completion.
   - Use the @complete tag to indicate the position where completion suggestions are expected.
    ```java
    @Test
    public void testPackageCompletionInsideMethodInvocations() {
        List<String> completed = complete("""
                // ... Java code snippet with @complete tag
            """);
        assertThat(completed).contains("ExpectedCompletion");
    }
```