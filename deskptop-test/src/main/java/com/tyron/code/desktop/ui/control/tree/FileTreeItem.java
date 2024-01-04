package com.tyron.code.desktop.ui.control.tree;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTreeItem extends FilterableTreeItem<Path> implements Comparable<FileTreeItem>{

    private final Path path;

    public FileTreeItem(Path path) {
        this.path = path;
        setValue(path);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public int compareTo(@NotNull FileTreeItem fileTreeItem) {
        // display directories first
        if (Files.isDirectory(path) && !Files.isDirectory(fileTreeItem.getPath())) {
            return -1;
        }
        if (!Files.isDirectory(path) && Files.isDirectory(fileTreeItem.getPath())) {
            return 1;
        }



        return path.compareTo(fileTreeItem.getPath());
    }
}
