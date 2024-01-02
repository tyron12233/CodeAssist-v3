package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;
import com.tyron.code.java.analysis.AnalysisResult;
import com.tyron.code.project.model.module.JavaModule;
import com.tyron.code.project.util.ModuleUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import shadow.com.sun.source.util.TreePath;
import shadow.com.sun.source.util.Trees;
import shadow.javax.lang.model.element.Element;

import java.util.ArrayList;
import java.util.List;

public class CompleteSymbolAction implements CompletionAction {


    @Override
    public ImmutableList<CompletionCandidate> getCompletionCandidates(CompletionArgs args) {
        ImmutableList.Builder<CompletionCandidate> builder = ImmutableList.builder();
        JavaModule module = args.module();

        List<ClassForImportCandidate> list = ModuleUtils.getAllClasses(module).stream()
                .parallel()
                .filter(it -> FuzzySearch.partialRatio(args.prefix(), it.getName()) >= 85)
                .map(it -> new ClassForImportCandidate(String.join(".", it.getPackageNameParts()), it.getSimpleName(), it.getSourceFileName()))
                .toList();
        builder.addAll(list);


        builder.addAll(completeUsingScope(args.currentAnalyzedPath(), args.analysisResult(), args.prefix()));
        return builder.build();
    }

    private List<CompletionCandidate> completeUsingScope(TreePath treePath, AnalysisResult analysisResult, String prefix) {
        analysisResult.analyzer().checkCancelled();
        List<CompletionCandidate> list = new ArrayList<>();
        var trees = Trees.instance(analysisResult.javacTask());
        var scope = trees.getScope(treePath);
        List<Element> elements = ScopeHelper.scopeMembers(analysisResult.javacTask(), scope, it -> it.toString().contains(prefix));
        for (Element element : elements) {
            list.add(new ElementCompletionCandidate(element));
        }
        return list;
    }

}
