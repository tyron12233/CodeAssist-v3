package com.tyron.code.project.file;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.tyron.code.project.model.TextRange;
import com.tyron.code.project.util.PathUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A simple implementation of {@link FileManager} that only loads file content
 * as-is.
 */
public class SimpleFileManager implements FileManager {

    private final Path rootPath;
    private final List<PathMatcher> ignorePathMatchers;
    private final Map<Path, String> snapshots;

    public SimpleFileManager() {
        this(Paths.get(".").toAbsolutePath().normalize(), ImmutableList.of());
    }

    public SimpleFileManager(Path rootPath, List<String> ignorePaths) {
        this.rootPath = rootPath;
        FileSystem fs = FileSystems.getDefault();
        this.ignorePathMatchers =
                ignorePaths.stream()
                        .map(fs::getPathMatcher)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
        this.snapshots = new HashMap<>();
    }

    @Override
    public void openFileForSnapshot(URI fileUri, String content) {
        snapshots.put(Paths.get(fileUri), content);
    }

    @Override
    public void applyEditToSnapshot(
            URI fileUri, TextRange editRange, Optional<Integer> rangeLength, String newText) {
        // No-op
    }

    @Override
    public void setSnapshotContent(URI fileUri, String newText) {
        Path path = Paths.get(fileUri);
        if (snapshots.containsKey(path)) {
            snapshots.put(path, newText);
        }
    }

    @Override
    public void closeFileForSnapshot(URI fileUri) {
        snapshots.remove(Paths.get(fileUri));
    }

    @Override
    public void watchSubDirectories(Path rootDirectory) {
        // No-op
    }

    @Override
    public void setFileChangeListener(FileChangeListener listener) {
        // No-op
    }

    @Override
    public Optional<EditHistory> getFileEditHistory(Path filePath) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Optional<CharSequence> getFileContent(Path filePath) {
        if (snapshots.containsKey(filePath.toAbsolutePath())) {
            return Optional.of(snapshots.get(filePath.toAbsolutePath()));
        }
        try {
            return Optional.of(Files.readString(filePath));
        } catch (IOException e) {
            // fall through.
        }
        return Optional.empty();
    }

    @Override
    public boolean isFileOpen(Path file) {
        return snapshots.containsKey(file);
    }

    @Override
    public void shutdown() {
        // No-op
    }

    @Override
    public boolean shouldIgnorePath(Path path) {
        return PathUtils.shouldIgnorePath(path, rootPath, ignorePathMatchers);
    }

    @Override
    public Path getProjectRootPath() {
        return rootPath;
    }
}