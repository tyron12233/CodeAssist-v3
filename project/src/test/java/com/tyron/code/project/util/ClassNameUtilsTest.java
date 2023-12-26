package com.tyron.code.project.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tyron.code.project.util.ClassNameUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassNameUtilsTest {

    @Test
    public void testGetFqn() {
        assertEquals("com.tyron.test.Test", getFqn("/com/tyron/test/Test.class"));
        assertEquals("com.tyron.test.Test", getFqn("com/tyron/test/Test.class"));
        assertEquals( "Test", getFqn("Test.class"));
    }

    @Test
    public void testGetClassName() {
        assertEquals( "Test", getClassName("com.tyron.test.Test"));
        assertEquals("Test", getClassName("Test"));
    }

    @Test
    public void testGetQualifiers() {
        assertEquals(
                List.of("com", "tyron", "test", "Test"),
                getAsQualifierList("com.tyron.test.Test")
        );
        assertEquals(
                List.of("Test"),
                getAsQualifierList("Test")
        );
        assertEquals(
                List.of("Test"),
                getAsQualifierList(".Test")
        );
        assertEquals(
                List.of("com", "tyron", "test"),
                getAsQualifierList(
                        "com.tyron.test.Test.class".substring(
                                0,
                                "com.tyron.test.Test.class".length() - ("Test".length() + ".class".length())
                        )
                )
        );
    }

    @Test
    public void testGetPackageOnly() {
        assertEquals(
                "com.tyron.test",
                getPackageOnly("com.tyron.test.Test")
        );
    }
}