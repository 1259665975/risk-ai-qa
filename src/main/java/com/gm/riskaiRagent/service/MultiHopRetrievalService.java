package com.gm.riskaiRagent.service;

import com.gm.riskaiRagent.config.RagProperties;
import com.gm.riskaiRagent.dto.MultiHopRetrievalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多跳检索服务。
 * <p>第 1 跳：用原始问题检索；后续跳：基于上一跳结果生成子查询再检索，合并去重后返回。
 * 当 {@code risk-ai.multi-hop.enabled=false} 时，直接委托单跳检索，行为与改造前一致。</p>
 */
@Slf4j
@Service
public class MultiHopRetrievalService {

    private static final String SUB_QUERY_SYSTEM = """
            你是风控知识库检索助手。根据用户问题和已检索片段，生成用于下一跳向量检索的补充查询。
            规则：
            1. 每行一条查询，不要编号，最多输出指定条数；
            2. 查询应聚焦片段中未充分覆盖的信息（如细则、流程、例外条款）；
            3. 若现有片段已足够，只输出一行：NONE
            """;

    private final VectorStoreService vectorStoreService;
    private final RagProperties ragProperties;
    private final ChatModel chatModel;

    public MultiHopRetrievalService(VectorStoreService vectorStoreService,
                                    RagProperties ragProperties,
                                    @Lazy ChatModel chatModel) {
        this.vectorStoreService = vectorStoreService;
        this.ragProperties = ragProperties;
        this.chatModel = chatModel;
    }

    /**
     * 执行检索：未开启多跳时等价于 {@link VectorStoreService#similaritySearch(String, List)}。
     */
    public MultiHopRetrievalResult retrieve(String question, List<Long> categoryIds) {
        RagProperties.MultiHop cfg = ragProperties.getMultiHop();
        if (!cfg.isEnabled()) {
            List<Document> docs = vectorStoreService.similaritySearch(question, categoryIds);
            return MultiHopRetrievalResult.builder()
                    .documents(docs)
                    .hops(1)
                    .multiHopUsed(false)
                    .build();
        }
        return multiHopRetrieve(question, categoryIds, cfg);
    }

    private MultiHopRetrievalResult multiHopRetrieve(String question, List<Long> categoryIds,
                                                     RagProperties.MultiHop cfg) {
        int maxHops = Math.max(2, cfg.getMaxHops());
        int hopTopK = Math.max(1, cfg.getHopTopK());
        int finalTopK = Math.max(1, cfg.getFinalTopK());

        Map<String, Document> merged = new LinkedHashMap<>();
        List<String> allSubQueries = new ArrayList<>();

        // 第 1 跳：原始问题
        List<Document> hop1 = safeSearch(question, categoryIds, hopTopK);
        mergeDocuments(merged, hop1);
        log.info("Multi-hop hop 1: query='{}', hits={}", truncate(question, 80), hop1.size());

        if (hop1.isEmpty()) {
            return MultiHopRetrievalResult.builder()
                    .documents(List.copyOf(merged.values()))
                    .hops(1)
                    .multiHopUsed(true)
                    .subQueries(allSubQueries)
                    .build();
        }

        int hopsExecuted = 1;
        List<Document> previousHopDocs = hop1;

        for (int hop = 2; hop <= maxHops; hop++) {
            List<String> subQueries = generateSubQueries(question, previousHopDocs, cfg);
            if (subQueries.isEmpty()) {
                log.info("Multi-hop hop {}: no sub-queries, stop", hop);
                break;
            }
            allSubQueries.addAll(subQueries);
            List<Document> hopDocs = new ArrayList<>();
            for (String subQuery : subQueries) {
                hopDocs.addAll(safeSearch(subQuery, categoryIds, hopTopK));
            }
            mergeDocuments(merged, hopDocs);
            hopsExecuted = hop;
            previousHopDocs = hopDocs;
            log.info("Multi-hop hop {}: subQueries={}, newHits={}", hop, subQueries, hopDocs.size());
            if (hopDocs.isEmpty()) {
                break;
            }
        }

        List<Document> finalDocs = merged.values().stream()
                .sorted(Comparator.comparing(Document::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(finalTopK)
                .collect(Collectors.toList());

        return MultiHopRetrievalResult.builder()
                .documents(finalDocs)
                .hops(hopsExecuted)
                .multiHopUsed(true)
                .subQueries(allSubQueries)
                .build();
    }

    private List<Document> safeSearch(String query, List<Long> categoryIds, int topK) {
        try {
            return vectorStoreService.similaritySearch(query, categoryIds, topK);
        } catch (Exception e) {
            log.warn("Multi-hop search failed, query={}", truncate(query, 80), e);
            return List.of();
        }
    }

    private List<String> generateSubQueries(String question, List<Document> docs, RagProperties.MultiHop cfg) {
        int limit = Math.max(1, cfg.getSubQueriesPerHop());
        if (cfg.isUseLlmSubQuery()) {
            try {
                return parseSubQueries(callLlmForSubQueries(question, docs, limit), limit);
            } catch (Exception e) {
                log.warn("LLM sub-query generation failed, fallback to template", e);
            }
        }
        return templateSubQueries(question, limit);
    }

    private String callLlmForSubQueries(String question, List<Document> docs, int limit) {
        String snippetSummary = docs.stream()
                .limit(3)
                .map(d -> truncate(d.getText(), 200))
                .collect(Collectors.joining("\n---\n"));

        String userContent = "最多输出 " + limit + " 条补充查询。\n\n用户问题：\n" + question
                + "\n\n已检索片段摘要：\n" + snippetSummary;

        ChatResponse response = chatModel.call(new Prompt(List.of(
                new SystemMessage(SUB_QUERY_SYSTEM),
                new UserMessage(userContent))));
        return response.getResult().getOutput().getText();
    }

    private List<String> parseSubQueries(String raw, int limit) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        List<String> queries = new ArrayList<>();
        for (String line : raw.split("\\R")) {
            String q = line.trim()
                    .replaceFirst("^\\d+[.、)）]\\s*", "")
                    .replaceFirst("^[-*]\\s*", "");
            if (!StringUtils.hasText(q) || "NONE".equalsIgnoreCase(q)) {
                continue;
            }
            queries.add(q);
            if (queries.size() >= limit) {
                break;
            }
        }
        return queries;
    }

    /** 规则模板扩展（不调用大模型时的兜底）。 */
    private List<String> templateSubQueries(String question, int limit) {
        List<String> templates = List.of(
                question + " 相关规定",
                question + " 审批流程",
                question + " 实施细则");
        return templates.stream().limit(limit).collect(Collectors.toList());
    }

    private void mergeDocuments(Map<String, Document> merged, List<Document> incoming) {
        for (Document doc : incoming) {
            String key = documentKey(doc);
            Document existing = merged.get(key);
            if (existing == null) {
                merged.put(key, doc);
            } else if (scoreOf(doc) > scoreOf(existing)) {
                merged.put(key, doc);
            }
        }
    }

    private String documentKey(Document doc) {
        if (StringUtils.hasText(doc.getId())) {
            return doc.getId();
        }
        Object source = doc.getMetadata().get("source");
        String text = doc.getText();
        return (source == null ? "" : source.toString()) + "#" + (text == null ? "" : text.hashCode());
    }

    private double scoreOf(Document doc) {
        return doc.getScore() == null ? 0.0 : doc.getScore();
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
