package com.tyron.code.java.parsing;

import com.google.auto.value.AutoValue;
import com.tyron.code.project.file.FileManager;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import shadow.com.sun.source.tree.ErroneousTree;
import shadow.com.sun.source.tree.LineMap;
import shadow.com.sun.source.tree.Tree;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.TreePathScanner;
import shadow.com.sun.tools.javac.tree.EndPosTable;
import shadow.com.sun.tools.javac.tree.JCTree;

import java.nio.file.Path;
import java.util.Optional;

@AutoValue
public abstract class PositionContext {

    public abstract Module getModule();

    public abstract Path getPath();

    public abstract TreePath getTreePath();

    /**
     * Gets position of the parsed content of the file.
     *
     * <p>Because the content of a file may be modified by {@link FileContentFixer}, the parsed
     * content may be different from the original content of the file. So this position can not be
     * used in the context of the original position.
     */
    public abstract long getPosition();

    public abstract EndPosTable getEndPosTable();

    public abstract CharSequence getContent();

}
