package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.project.model.*;
import com.tyron.code.project.model.Module;
import com.tyron.code.project.util.JarReader;
import com.tyron.code.project.util.ModuleUtils;
import shadow.com.sun.source.tree.MemberSelectTree;
import shadow.com.sun.source.tree.Scope;
import shadow.com.sun.source.tree.Tree;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.Trees;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.code.Type;
import shadow.javax.lang.model.element.ElementKind;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.element.Modifier;
import shadow.javax.lang.model.element.TypeElement;
import shadow.javax.lang.model.type.ArrayType;
import shadow.javax.lang.model.type.DeclaredType;
import shadow.javax.lang.model.type.TypeVariable;

import java.util.*;

public class CompleteMemberSelectAction implements CompletionAction {
    @Override
    public ImmutableList<CompletionCandidate> getCompletionCandidates(CompletionArgs args) {
        JavacTaskImpl task = args.analysisResult().javacTask();
        TreePath path = args.currentAnalyzedPath();
        var trees = Trees.instance(task);
        var select = (MemberSelectTree) path.getLeaf();

        path = new TreePath(path, select.getExpression());
        var isStatic = trees.getElement(path) instanceof TypeElement;
        var scope = trees.getScope(path);
        var type = trees.getTypeMirror(path);

        Tree parent = path.getParentPath().getParentPath().getLeaf();
        boolean endsWithParen = parent.getKind() == Tree.Kind.METHOD_INVOCATION;

        if (type instanceof ArrayType) {
            return completeArrayMemberSelect(isStatic);
        } else if (type instanceof TypeVariable) {
            return completeTypeVariableMemberSelect(
                    args.analysisResult(),
                    scope,
                    (TypeVariable) type,
                    isStatic,
                    args.prefix(),
                    endsWithParen
            );
        } else if (type instanceof DeclaredType) {
            return completeDeclaredTypeMemberSelect(
                    args.analysisResult(),
                    scope,
                    (DeclaredType) type,
                    isStatic,
                    args.prefix(),
                    endsWithParen
            );
        } else if (type instanceof Type.PackageType) {
            return completePackage(
                    args.analysisResult(),
                    scope,
                    (Type.PackageType) type,
                    args.prefix()
            );
        }

        return ImmutableList.of();
    }

    private ImmutableList<CompletionCandidate> completePackage(AnalysisResult analysisResult, Scope scope, Type.PackageType type, String prefix) {
        ProjectModule module = analysisResult.module();
        List<Module> dependenciesRecursive = ModuleUtils.getDependenciesRecursive(module);

        List<String> asQualifierList = JarReader.getAsQualifierList(type.toString());
        List<PackageScope> packageScopes = dependenciesRecursive.stream()
                .filter(dependency -> dependency instanceof ModuleWithSourceFiles)
                .map(dependency -> (ModuleWithSourceFiles) dependency)
                .flatMap(dependency -> dependency.getPackage(asQualifierList).stream())
                .filter(Objects::nonNull)
                .toList();

        ImmutableList.Builder<CompletionCandidate> builder = ImmutableList.builder();
        for (PackageScope packageScope : packageScopes) {
            Set<UnparsedJavaFile> files = packageScope.getFiles();
            List<PackageScope> subPackages = packageScope.getSubPackages();

            files.stream().map(it -> new SimpleCompletionCandidate(it.fileName())).forEach(builder::add);
            subPackages.stream().map(it -> new SimpleCompletionCandidate(it.getSimpleName())).forEach(builder::add);
        }
        return builder.build();
    }

    private ImmutableList<CompletionCandidate> completeArrayMemberSelect(boolean isStatic) {
        if (isStatic) {
            return ImmutableList.of();
        }

        return ImmutableList.of(KeywordCompletionCandidate.LENGTH);
    }

    private ImmutableList<CompletionCandidate> completeTypeVariableMemberSelect(AnalysisResult analysisResult, Scope scope, TypeVariable type, boolean isStatic, String partial, boolean endsWithParen) {
        if (type.getUpperBound() instanceof DeclaredType) {
            return completeDeclaredTypeMemberSelect(
                    analysisResult,
                    scope,
                    (DeclaredType) type.getUpperBound(),
                    isStatic,
                    partial,
                    endsWithParen
            );
        } else if (type.getUpperBound() instanceof TypeVariable) {
            return completeTypeVariableMemberSelect(
                    analysisResult,
                    scope,
                    (TypeVariable) type.getUpperBound(),
                    isStatic,
                    partial,
                    endsWithParen
            );
        }

        return ImmutableList.of();
    }

    private ImmutableList<CompletionCandidate> completeDeclaredTypeMemberSelect(
            AnalysisResult analysisResult, Scope scope, DeclaredType type, boolean isStatic, String partial, boolean endsWithParen) {
        JavacTaskImpl task = analysisResult.javacTask();
        var trees = Trees.instance(task);
        var typeElement = (TypeElement) type.asElement();
        var list = new ArrayList<CompletionCandidate>();
        var methods = new HashMap<String, List<ExecutableElement>>();

        task.getElements().getAllMembers(typeElement).stream()
                .parallel()
                .filter(member -> member.getKind() != ElementKind.CONSTRUCTOR)
                .filter(member -> CompletionPrefixUtils.prefixPartiallyMatch(partial, member.getSimpleName().toString()))
                .filter(member -> trees.isAccessible(scope, member, type))
                .filter(member -> isStatic == member.getModifiers().contains(Modifier.STATIC))
                .forEach(member -> {
                    if (member.getKind() == ElementKind.METHOD) {
                        putMethod((ExecutableElement) member, methods);
                    } else {
                        list.add(new ElementCompletionCandidate(member));
                    }
                });
        methods.values().stream()
                .map(overloads -> method(analysisResult, overloads, !endsWithParen))
                .forEach(list::add);

        if (isStatic) {
            list.add(KeywordCompletionCandidate.CLASS);
        }

        if (isStatic && isEnclosingClass(type, scope)) {
            list.add(KeywordCompletionCandidate.THIS);
            list.add(KeywordCompletionCandidate.SUPER);
        }
        return ImmutableList.copyOf(list);
    }

    private boolean isEnclosingClass(DeclaredType type, Scope start) {
        for (var s : ScopeHelper.fastScopes(start)) {
            // If we reach a static method, stop looking
            var method = s.getEnclosingMethod();
            if (method != null && method.getModifiers().contains(Modifier.STATIC)) {
                return false;
            }
            // If we find the enclosing class
            var thisElement = s.getEnclosingClass();
            if (thisElement != null && thisElement.asType().equals(type)) {
                return true;
            }
            // If the enclosing class is static, stop looking
            if (thisElement != null && thisElement.getModifiers().contains(Modifier.STATIC)) {
                return false;
            }
        }
        return false;
    }

    private void putMethod(ExecutableElement method, Map<String, List<ExecutableElement>> methods) {
        var name = method.getSimpleName().toString();
        if (!methods.containsKey(name)) {
            methods.put(name, new ArrayList<>());
        }
        methods.get(name).add(method);
    }

    private CompletionCandidate method(AnalysisResult analysisResult, List<ExecutableElement> overloads, boolean addParens) {
        var first = overloads.get(0);
        ElementCompletionCandidate candidate = new ElementCompletionCandidate(first);
        candidate.putData("ADD_PARENS", false);
        return candidate;
    }
}
