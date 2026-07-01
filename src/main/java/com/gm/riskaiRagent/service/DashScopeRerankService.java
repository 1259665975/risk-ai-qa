package com.gm.riskaiRagent.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gm.riskaiRagent.config.RagProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 调用百炼 qwen3-rerank 对候选文档精排。
 */
@Slf4j
@Service
public class DashScopeRerankService {

    private final RestClient restClient;
    private final RagProperties ragProperties;
    private final String apiKey;

    public DashScopeRerankService(RagProperties ragProperties,
                                  @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.ragProperties = ragProperties;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().build();
    }

    public List<Document> rerank(String query, List<Document> candidates, int topN) {
        if (!StringUtils.hasText(query) || candidates == null || candidates.isEmpty()) {
            return candidates == null ? List.of() : candidates;
        }
        if (!StringUtils.hasText(apiKey)) {
            log.warn("DashScope API key missing, skip rerank");
            return limit(candidates, topN);
        }

        RagProperties.Rerank cfg = ragProperties.getRetrieval().getRerank();
        List<String> documents = candidates.stream()
                .map(Document::getText)
                .map(text -> text == null ? "" : text)
                .toList();

        RerankRequest request = new RerankRequest();
        request.setModel(cfg.getModel());
        request.setQuery(query);
        request.setDocuments(documents);
        request.setTopN(Math.min(Math.max(1, topN), documents.size()));
        request.setInstruct(cfg.getInstruct());

        try {
            RerankResponse response = restClient.post()
                    .uri(cfg.getBaseUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(RerankResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("Rerank returned empty results, fallback to original ranking");
                return limit(candidates, topN);
            }

            List<Document> reranked = new ArrayList<>();
            for (RerankResultItem item : response.getResults()) {
                if (item.getIndex() == null || item.getIndex() < 0 || item.getIndex() >= candidates.size()) {
                    continue;
                }
                Document original = candidates.get(item.getIndex());
                reranked.add(Document.builder()
                        .id(original.getId())
                        .text(original.getText())
                        .metadata(original.getMetadata())
                        .score(item.getRelevanceScore())
                        .build());
            }
            if (reranked.isEmpty()) {
                return limit(candidates, topN);
            }
            log.info("Rerank completed: candidates={}, returned={}", candidates.size(), reranked.size());
            return reranked;
        } catch (Exception e) {
            log.warn("Rerank API call failed: {}", e.getMessage());
            if (cfg.isFailOpen()) {
                return limit(candidates, topN);
            }
            throw e;
        }
    }

    private List<Document> limit(List<Document> docs, int topN) {
        return docs.stream().limit(Math.max(1, topN)).toList();
    }

    @Data
    static class RerankRequest {
        private String model;
        private String query;
        private List<String> documents;
        @JsonProperty("top_n")
        private int topN;
        private String instruct;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RerankResponse {
        private List<RerankResultItem> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RerankResultItem {
        private Integer index;
        @JsonProperty("relevance_score")
        private Double relevanceScore;
    }
}
