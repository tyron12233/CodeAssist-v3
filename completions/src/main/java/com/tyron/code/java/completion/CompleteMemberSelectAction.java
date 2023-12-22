package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.AnalysisResult;
import shadow.com.sun.source.tree.MemberSelectTree;
import shadow.com.sun.source.tree.Scope;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.Trees;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.javax.lang.model.element.ElementKind;
import shadow.javax.lang.model.element.ExecutableElement;
import shadow.javax.lang.model.element.Modifier;
import shadow.javax.lang.model.element.TypeElement;
import shadow.javax.lang.model.type.ArrayType;
import shadow.javax.lang.model.type.DeclaredType;
import shadow.javax.lang.model.type.TypeVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (type instanceof ArrayType) {
            return completeArrayMemberSelect(isStatic);
        } else if (type instanceof TypeVariable) {
            return completeTypeVariableMemberSelect(args.analysisResult(), scope, (TypeVariable) type, isStatic, args.prefix(), false);
        } else if (type instanceof DeclaredType) {
            return completeDeclaredTypeMemberSelect(args.analysisResult(), scope, (DeclaredType) type, isStatic, args.prefix(), false);
        } else {
            return ImmutableList.of();
        }
    }

    private ImmutableList<CompletionCandidate> completeArrayMemberSelect(boolean isStatic) {
        if (isStatic) {
            return ImmutableList.of();
        } else {
            return ImmutableList.of(KeywordCompletionCandidate.LENGTH);
        }
    }

    private ImmutableList<CompletionCandidate> completeTypeVariableMemberSelect(AnalysisResult analysisResult, Scope scope, TypeVariable type, boolean isStatic, String partial, boolean endsWithParen) {
        if (type.getUpperBound() instanceof DeclaredType) {
            return completeDeclaredTypeMemberSelect(analysisResult, scope, (DeclaredType) type.getUpperBound(), isStatic, partial, endsWithParen);
        } else if (type.getUpperBound() instanceof TypeVariable) {
            return completeTypeVariableMemberSelect(analysisResult, scope, (TypeVariable) type.getUpperBound(), isStatic, partial, endsWithParen);
        } else {
            return ImmutableList.of();
        }
    }

    private ImmutableList<CompletionCandidate> completeDeclaredTypeMemberSelect(
            AnalysisResult analysisResult, Scope scope, DeclaredType type, boolean isStatic, String partial, boolean endsWithParen) {
        JavacTaskImpl task = analysisResult.javacTask();
        var trees = Trees.instance(task);
        var typeElement = (TypeElement) type.asElement();
        var list = new ArrayList<CompletionCandidate>();
        var methods = new HashMap<String, List<ExecutableElement>>();
        for (var member : task.getElements().getAllMembers(typeElement)) {
            if (member.getKind() == ElementKind.CONSTRUCTOR) continue;
//            if (!StringSearch.matchesPartialName(member.getSimpleName(), partial)) continue;
            if (!trees.isAccessible(scope, member, type)) continue;
            if (isStatic != member.getModifiers().contains(Modifier.STATIC)) continue;
            if (member.getKind() == ElementKind.METHOD) {
                putMethod((ExecutableElement) member, methods);
            } else {
                list.add(new ElementCompletionCandidate(member));
            }
        }

        for (var overloads : methods.values()) {
            list.add(method(analysisResult, overloads, !endsWithParen));
        }
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
        return new ElementCompletionCandidate(first);
    }
}
