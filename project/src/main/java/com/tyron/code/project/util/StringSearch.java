package com.tyron.code.project.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class StringSearch {

    public static String packageName(String contents) {
        return packageName(new BufferedReader(new StringReader(contents)));
    }

    public static String packageName(Path file) {
        try {
            return packageName(Files.newBufferedReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String packageName(BufferedReader reader) {
        var packagePattern = Pattern.compile("^package +(.*);");
        var startOfClass = Pattern.compile("^[\\w ]*class +\\w+");
        try (reader) {
            for (var line = reader.readLine(); line != null; line = reader.readLine()) {
                if (startOfClass.matcher(line).find()) return "";
                var matchPackage = packagePattern.matcher(line);
                if (matchPackage.matches()) {
                    return matchPackage.group(1);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO fall back on parsing file
        return "";
    }
}
