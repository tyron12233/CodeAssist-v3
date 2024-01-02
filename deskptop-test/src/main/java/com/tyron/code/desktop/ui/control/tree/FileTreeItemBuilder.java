package com.tyron.code.desktop.ui.control.tree;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileTreeItemBuilder {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/home/tyronscott/IdeaProjects/CodeAssistCompletions/deskptop-test");
        FileTreeItem build = build(path);
        System.out.println();
    }

    public static FileTreeItem build(Path root) throws IOException {
        FileVisitor visitor = new FileVisitor();
        Files.walkFileTree(root, visitor);
        return visitor.getRoot();
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {
        private FileTreeItem current;
        private FileTreeItem root;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (current == null) {
                current = new FileTreeItem(dir);
                root = current;
            } else {
                FileTreeItem item = new FileTreeItem(dir);
                current.addAndSortChild(item);
                current = item;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            FileTreeItem item = new FileTreeItem(file);
            current.addAndSortChild(item);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            current = (FileTreeItem) current.getParent();
            return FileVisitResult.CONTINUE;
        }

        public FileTreeItem getRoot() {
            return root;
        }
    }
}

