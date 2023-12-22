package com.tyron.code.java.completion;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CompletionCandidateListBuilder {

    private final Map<String, CompletionCandidateWithMatchLevel> candidateMap;
    private final String completionPrefix;

    public CompletionCandidateListBuilder(String completionPrefix) {
        candidateMap = new HashMap<>();
        this.completionPrefix = completionPrefix;
    }

    public boolean hasCandidateWithName(String name) {
        return candidateMap.containsKey(name);
    }

//    public CompletionCandidateListBuilder addEntities(
//            Multimap<String, Entity> entities, CompletionCandidate.SortCategory sortCategory) {
//        for (Entity entity : entities.values()) {
//            addEntity(entity, sortCategory);
//        }
//        return this;
//    }

    public CompletionCandidateListBuilder addCandidates(Collection<CompletionCandidate> candidates) {
        for (CompletionCandidate candidate : candidates) {
            addCandidate(candidate);
        }
        return this;
    }

//    public CompletionCandidateListBuilder addEntity(
//            Entity entity, CompletionCandidate.SortCategory sortCategory) {
//        return this.addCandidate(new EntityCompletionCandidate(entity, sortCategory));
//    }

    public CompletionCandidateListBuilder addCandidate(CompletionCandidate candidate) {
        String name = candidate.getName();
        CompletionPrefixMatcher.MatchLevel matchLevel =
                CompletionPrefixMatcher.computeMatchLevel(name, completionPrefix);
        if (matchLevel == CompletionPrefixMatcher.MatchLevel.NOT_MATCH) {
            return this;
        }

//        if (!candidateMap.containsKey(name)) {
//            candidateMap.put(name, new EntityShadowingListBuilder<>(GET_ELEMENT_FUNCTION));
//        }
        candidateMap.put(name, CompletionCandidateWithMatchLevel.create(candidate, matchLevel));
        return this;
    }

    public ImmutableList<CompletionCandidate> build() {
        return candidateMap.values().stream()
//                .flatMap(EntityShadowingListBuilder::stream)
                .sorted()
                .map(CompletionCandidateWithMatchLevel::getCompletionCandidate)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
}