package com.gm.riskaiRagent.util;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reciprocal Rank Fusion (RRF) 多路检索结果融合。
 */
public final class ReciprocalRankFusion {

    private ReciprocalRankFusion() {
    }

    public static List<Document> fuse(List<List<Document>> rankedLists, int rrfK, int limit) {
        if (rankedLists == null || rankedLists.isEmpty()) {
            return List.of();
        }
        int k = Math.max(1, rrfK);
        int max = Math.max(1, limit);

        Map<String, FusionEntry> fused = new LinkedHashMap<>();
        for (List<Document> rankedList : rankedLists) {
            if (rankedList == null || rankedList.isEmpty()) {
                continue;
            }
            for (int rank = 0; rank < rankedList.size(); rank++) {
                Document doc = rankedList.get(rank);
                if (doc == null) {
                    continue;
                }
                String key = documentKey(doc);
                FusionEntry entry = fused.computeIfAbsent(key, ignored -> new FusionEntry(doc));
                entry.score += 1.0 / (k + rank + 1);
            }
        }

        return fused.values().stream()
                .sorted(Comparator.comparingDouble((FusionEntry e) -> e.score).reversed())
                .limit(max)
                .map(entry -> Document.builder()
                        .id(entry.document.getId())
                        .text(entry.document.getText())
                        .metadata(entry.document.getMetadata())
                        .score(entry.score)
                        .build())
                .toList();
    }

    private static String documentKey(Document doc) {
        if (doc.getId() != null && !doc.getId().isBlank()) {
            return doc.getId();
        }
        Object source = doc.getMetadata().get("source");
        String text = doc.getText();
        return (source == null ? "" : source.toString()) + "#" + (text == null ? "" : text.hashCode());
    }

    private static final class FusionEntry {
        private final Document document;
        private double score;

        private FusionEntry(Document document) {
            this.document = document;
        }
    }
}
