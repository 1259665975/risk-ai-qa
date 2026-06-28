package com.gm.riskaiRagent.service;

import com.gm.riskaiRagent.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    public void add(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        vectorStore.add(documents);
        log.info("Added {} chunks to vector store", documents.size());
    }

    public List<Document> similaritySearch(String query) {
        return similaritySearch(query, null);
    }

    public List<Document> similaritySearch(String query, List<Long> categoryIds) {
        return similaritySearch(query, categoryIds, ragProperties.getRag().getTopK());
    }

    /**
     * 向量相似度检索（可指定 topK，供多跳检索每一跳使用）。
     */
    public List<Document> similaritySearch(String query, List<Long> categoryIds, int topK) {
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(Math.max(1, topK))
                .similarityThreshold(ragProperties.getRag().getSimilarityThreshold());

        String filter = buildCategoryFilter(categoryIds);
        if (filter != null) {
            builder.filterExpression(filter);
        }

        return vectorStore.similaritySearch(builder.build());
    }

    public void delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        vectorStore.delete(ids);
        log.info("Deleted {} chunks from vector store", ids.size());
    }

    private String buildCategoryFilter(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return categoryIds.stream()
                .map(id -> "categoryId == '" + id + "'")
                .collect(Collectors.joining(" || "));
    }
}
