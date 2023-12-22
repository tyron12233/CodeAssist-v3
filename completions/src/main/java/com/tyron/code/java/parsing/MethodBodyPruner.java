package com.tyron.code.java.parsing;

import shadow.com.sun.source.tree.MethodTree;
import shadow.com.sun.tools.javac.tree.JCTree;
import shadow.com.sun.tools.javac.tree.TreeMaker;
import shadow.com.sun.tools.javac.tree.TreeTranslator;
import shadow.com.sun.tools.javac.util.Context;
import shadow.com.sun.tools.javac.util.List;

public class MethodBodyPruner extends TreeTranslator {

    private final TreeMaker treeMaker;
    public MethodBodyPruner() {
        treeMaker = TreeMaker.instance(new Context());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends JCTree> T translate(T tree) {
        if (tree instanceof JCTree.JCMethodDecl decl) {
            return (T) treeMaker.MethodDef(
                    decl.mods,
                    decl.name,
                    decl.restype,
                    decl.typarams,
                    decl.params,
                    decl.thrown,
                    treeMaker.Block(0, List.nil()),
                    decl.defaultValue
            );
        }
        return super.translate(tree);
    }
}
