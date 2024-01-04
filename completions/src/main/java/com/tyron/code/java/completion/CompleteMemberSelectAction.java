package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.info.ClassInfo;
import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.project.model.JavaFileInfo;
import com.tyron.code.project.model.PackageScope;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.model.module.Module;
import com.tyron.code.project.model.module.SourceModule;
import com.tyron.code.project.util.ClassNameUtils;
import com.tyron.code.project.util.ModuleUtils;
import shadow.com.sun.source.tree.ImportTree;
import shadow.com.sun.source.tree.MemberSelectTree;
import shadow.com.sun.source.tree.Scope;
import shadow.com.sun.source.tree.Tree;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.Trees;
import shadow.com.sun.tools.javac.api.JavacTaskImpl;
import shadow.com.sun.tools.javac.code.Type;
import shadow.javax.lang.model.element.*;
import shadow.javax.lang.model.type.*;
import shadow.javax.lang.model.util.Types;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class CompleteMemberSelectAction implements CompletionAction {
    @Override
    public ImmutableList<CompletionCandidate> getCompletionCandidates(CompletionArgs args) {
        JavacTaskImpl task = args.analysisResult().javacTask();
        TreePath path = args.currentAnalyzedPath();
        var trees = Trees.instance(task);

        MemberSelectTree select;
        if (path.getLeaf() instanceof ImportTree importTree) {
            select = (MemberSelectTree) importTree.getQualifiedIdentifier();
        } else {
            select = (MemberSelectTree) path.getLeaf();
        }

        path = new TreePath(path, select.getExpression());
        var isStatic = trees.getElement(path) instanceof TypeElement;
        var scope = trees.getScope(path);
        var type = trees.getTypeMirror(path);

        Tree parent = path.getParentPath().getParentPath().getLeaf();
        boolean endsWithParen = parent.getKind() == Tree.Kind.METHOD_INVOCATION;

        // when inside a method invocation,
        // typing a package expression (java.util.something) the compiler infers it to be
        // an error type. we change the type to a package if it matches any package
        if (type.getKind() == TypeKind.ERROR) {
            PackageElement packageElement = task.getElements().getPackageElement(type.toString());
            if (packageElement != null) {
                type = packageElement.asType();
            }
        }

        /*
         * The user is likely trying to complete a static method from
         * a non-imported class, so we try to find the class and import it
         */
        if (type.getKind() == TypeKind.PACKAGE && !type.toString().contains(".") && Character.isUpperCase(type.toString().charAt(0))) {
            String className = type.toString();
            JavaModule module = args.analysisResult().module();
            List<ClassInfo> list = ModuleUtils.getAllClasses(module).stream()
                    .filter(classInfo -> classInfo.getSimpleName().equals(className))
                    .toList();
            if (!list.isEmpty()) {
                ClassInfo classInfo = list.get(0);
                TypeElement typeElement = task.getElements().getTypeElement(
                        classInfo.getName().replace('/', '.')
                );
                if (typeElement != null) {
                    type = typeElement.asType();
                }
            }
        }

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
        String packageName = type.toString().replace('.', '/');
        final String[] packageParts;
        if (!packageName.contains("/")) {
            packageParts = new String[]{packageName};
        } else {
            packageParts = packageName.split("/");
        }

        Set<ClassInfo> allClasses = ModuleUtils.getAllClasses(analysisResult.module());

        Set<CompletionCandidate> classes = allClasses.stream()
                .filter(it -> packageName.equals(it.getPackageName()))
                .map(it -> new SimpleCompletionCandidate(it.getSimpleName()))
                .collect(Collectors.toSet());

        Set<CompletionCandidate> packages = allClasses.stream()
                .filter(it -> it.getPackageName() != null)
                .filter(it -> it.getPackageName().startsWith(packageName))
                .map(ClassInfo::getPackageNameParts)
                .filter(it -> it.length == packageParts.length + 1)
                .map(it -> it[it.length - 1])
                .distinct()
                .map(SimpleCompletionCandidate::new)
                .collect(Collectors.toSet());

        return ImmutableList.<CompletionCandidate>builder()
                .addAll(classes)
                .addAll(packages)
                .build();
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
                .filter(member -> member.getKind() != ElementKind.CONSTRUCTOR)
                .filter(member -> CompletionPrefixUtils.prefixPartiallyMatch(partial, member.getSimpleName().toString()))
                .filter(member -> trees.isAccessible(scope, member, type))
                .forEach(member -> {
                    if (isStatic) {
                        if (!member.getModifiers().contains(Modifier.STATIC)) {
                            return;
                        }
                    }
                    if (member.getKind() == ElementKind.METHOD) {
                        putMethod((ExecutableElement) member, methods);
                    } else {
                        list.add(new ElementCompletionCandidate(member));
                    }
                });

        methods.values().stream()
                .map(overloads -> method(analysisResult, type, overloads, !endsWithParen))
                .flatMap(Collection::stream)
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

    private List<CompletionCandidate> method(AnalysisResult analysisResult, DeclaredType type, List<ExecutableElement> overloads, boolean addParens) {
        JavacTaskImpl task = analysisResult.javacTask();
        Types types = task.getTypes();
        return overloads.stream()
                .map(method -> {
                    TypeMirror memberOf = types.asMemberOf(type, method);
                    return new MethodCompletionCandidate(
                            method,
                            ((ExecutableType) memberOf)
                    );
                })
                .map(it -> (CompletionCandidate) it)
                .toList();
    }
}