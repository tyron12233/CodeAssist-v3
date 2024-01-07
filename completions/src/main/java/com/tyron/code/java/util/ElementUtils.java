package com.tyron.code.java.util;

import com.tyron.code.java.analysis.AnalysisResult;
import shadow.com.sun.source.util.JavacTask;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.code.Kinds;
import shadow.com.sun.tools.javac.code.Symbol;
import shadow.com.sun.tools.javac.code.Symtab;
import shadow.com.sun.tools.javac.comp.Check;
import shadow.com.sun.tools.javac.util.Context;
import shadow.com.sun.tools.javac.util.Name;
import shadow.com.sun.tools.javac.util.Names;
import shadow.javax.lang.model.element.ModuleElement;
import shadow.javax.lang.model.element.TypeElement;

import java.util.Set;

public class ElementUtils {

    public static TypeElement getTypeElementByBinaryName(AnalysisResult info, String name) {
        return getTypeElementByBinaryName(info.javacTask(), name);
    }

    public static TypeElement getTypeElementByBinaryName(JavacTask task, String name) {
        Set<? extends ModuleElement> allModules = task.getElements().getAllModuleElements();
        Context ctx = ((JavacTaskImpl) task).getContext();
        Symtab syms = Symtab.instance(ctx);

        if (allModules.isEmpty()) {
            return getTypeElementByBinaryName(task, syms.noModule, name);
        }

        TypeElement result = null;
        boolean foundInUnamedModule = false;

        for (ModuleElement me : allModules) {
            TypeElement found = getTypeElementByBinaryName(task, me, name);

            if (result == found) {
                // avoid returning null, partial fix for [NETBEANS-4832]
                continue;
            }

            if (found != null) {
                if ((Symbol.ModuleSymbol) me == syms.unnamedModule) {
                    foundInUnamedModule = true;
                }
                if (result != null) {
                    if (foundInUnamedModule == true) {
                        for (TypeElement elem : new TypeElement[]{result, found}) {
                            if ((elem.getKind().isClass() || elem.getKind().isInterface())
                                && (((Symbol.ClassSymbol) elem).packge().modle != syms.unnamedModule)) {
                                return elem;
                            }
                        }
                    } else {
                        return null;
                    }
                }
                result = found;
            }
        }

        return result;
    }

    public static TypeElement getTypeElementByBinaryName(AnalysisResult info, ModuleElement mod, String name) {
        return getTypeElementByBinaryName(info.javacTask(), mod, name);
    }

    public static TypeElement getTypeElementByBinaryName(JavacTask task, ModuleElement mod, String name) {
        Context ctx = ((JavacTaskImpl) task).getContext();
        Names names = Names.instance(ctx);
        Symtab syms = Symtab.instance(ctx);
        Check chk = Check.instance(ctx);
        final Name wrappedName = names.fromString(name);
        Symbol.ClassSymbol clazz = chk.getCompiled((Symbol.ModuleSymbol) mod, wrappedName);
        if (clazz != null) {
            return clazz;
        }
        clazz = syms.enterClass((Symbol.ModuleSymbol) mod, wrappedName);

        try {
            clazz.complete();

            if (clazz.kind == Kinds.Kind.TYP &&
                clazz.flatName() == wrappedName) {
                return clazz;
            }
        } catch (Symbol.CompletionFailure ignored) {
        }

        return null;
    }

}
