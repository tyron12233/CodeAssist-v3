package com.tyron.code.project.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JarReaderTest {

    @Test
    public void testGetFqn() {
        assertEquals("com.tyron.test.Test", JarReader.getFqn("/com/tyron/test/Test.class"));
        assertEquals("com.tyron.test.Test", JarReader.getFqn("com/tyron/test/Test.class"));
        assertEquals( "Test", JarReader.getFqn("Test.class"));
    }

    @Test
    public void testGetClassName() {
        assertEquals( "Test", JarReader.getClassName("com.tyron.test.Test"));
        assertEquals("Test", JarReader.getClassName("Test"));
    }

    @Test
    public void testGetQualifiers() {
        assertEquals(
                List.of("com", "tyron", "test", "Test"),
                JarReader.getAsQualifierList("com.tyron.test.Test")
        );
        assertEquals(
                List.of("Test"),
                JarReader.getAsQualifierList("Test")
        );
        assertEquals(
                List.of("Test"),
                JarReader.getAsQualifierList(".Test")
        );
        assertEquals(
                List.of("com", "tyron", "test"),
                JarReader.getAsQualifierList(
                        "com.tyron.test.Test.class".substring(
                                0,
                                "com.tyron.test.Test.class".length() - ("Test".length() + ".class".length())
                        )
                )
        );
    }

}