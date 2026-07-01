package com.gm.riskaiRagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redis 关键词检索索引：入库时同步 chunk 文本，供混合检索 BM25 召回。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkIndexService {

    static final String INDEX_HASH = "risk-ai:chunk:index";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void index(String chunkId, String text, Long categoryId, String source) {
        if (chunkId == null || chunkId.isBlank() || text == null) {
            return;
        }
        try {
            ChunkIndexEntry entry = new ChunkIndexEntry(text, categoryId == null ? null : String.valueOf(categoryId), source);
            stringRedisTemplate.opsForHash().put(INDEX_HASH, chunkId, objectMapper.writeValueAsString(entry));
        } catch (JsonProcessingException e) {
            log.warn("Failed to index chunk {}, skipped keyword index", chunkId, e);
        }
    }

    public void remove(Collection<String> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForHash().delete(INDEX_HASH, chunkIds.toArray());
    }

    public void clear() {
        stringRedisTemplate.delete(INDEX_HASH);
    }

    /**
     * 返回全部已索引 chunk（category 过滤在调用方完成）。
     */
    public Map<String, ChunkIndexEntry> listAll() {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(INDEX_HASH);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ChunkIndexEntry> result = new LinkedHashMap<>(raw.size());
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            String chunkId = String.valueOf(entry.getKey());
            ChunkIndexEntry parsed = parseEntry(entry.getValue());
            if (parsed != null) {
                result.put(chunkId, parsed);
            }
        }
        return result;
    }

    private ChunkIndexEntry parseEntry(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value.toString(), ChunkIndexEntry.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse chunk index entry: {}", value, e);
            return null;
        }
    }

    public record ChunkIndexEntry(String text, String categoryId, String source) {
    }
}
