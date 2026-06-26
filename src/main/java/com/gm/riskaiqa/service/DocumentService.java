package com.gm.riskaiqa.service;

import com.gm.riskaiqa.config.RagProperties;
import com.gm.riskaiqa.util.DocumentParser;
import com.gm.riskaiqa.util.TokenTextChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.gm.riskaiqa.dto.IngestResponse;

/**
 * Module 1 - Knowledge ingestion.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String CHUNK_ID_SET = "risk-ai:doc:chunk-ids";
    private static final String DOC_CHUNK_SET_PREFIX = "risk-ai:doc:chunks:";

    private final DocumentParser documentParser;
    private final TokenTextChunker tokenTextChunker;
    private final VectorStoreService vectorStoreService;
    private final StringRedisTemplate stringRedisTemplate;

    public IngestResponse ingest(MultipartFile file) throws IOException {
        return ingest(file, null);
    }

    public IngestResponse ingest(MultipartFile file, Long categoryId) throws IOException {
        String fileName = file.getOriginalFilename();
        String docId = UUID.randomUUID().toString().replace("-", "");

        String content = documentParser.parse(file.getInputStream(), fileName);
        int tokenCount = tokenTextChunker.countTokens(content);
        List<String> chunks = tokenTextChunker.split(content);

        List<Document> documents = new ArrayList<>(chunks.size());
        List<String> chunkIds = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkId = UUID.randomUUID().toString().replace("-", "");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("docId", docId);
            metadata.put("source", fileName);
            metadata.put("chunkIndex", i);
            if (categoryId != null) {
                metadata.put("categoryId", String.valueOf(categoryId));
            }
            documents.add(Document.builder()
                    .id(chunkId)
                    .text(chunks.get(i))
                    .metadata(metadata)
                    .build());
            chunkIds.add(chunkId);
        }

        vectorStoreService.add(documents);

        if (!chunkIds.isEmpty()) {
            stringRedisTemplate.opsForSet().add(CHUNK_ID_SET, chunkIds.toArray(new String[0]));
            stringRedisTemplate.opsForSet().add(docChunkKey(docId), chunkIds.toArray(new String[0]));
        }

        log.info("Ingested '{}' docId={} categoryId={} chunks={}", fileName, docId, categoryId, chunks.size());

        return IngestResponse.builder()
                .fileName(fileName)
                .docId(docId)
                .charCount(content.length())
                .tokenCount(tokenCount)
                .chunkCount(chunks.size())
                .build();
    }

    public void deleteByDocId(String docId) {
        if (docId == null || docId.isBlank()) {
            return;
        }
        Set<String> ids = stringRedisTemplate.opsForSet().members(docChunkKey(docId));
        if (ids != null && !ids.isEmpty()) {
            vectorStoreService.delete(new ArrayList<>(ids));
            stringRedisTemplate.opsForSet().remove(CHUNK_ID_SET, ids.toArray());
            stringRedisTemplate.delete(docChunkKey(docId));
            log.info("Deleted docId={} chunks={}", docId, ids.size());
        }
    }

    public long clearAll() {
        Set<String> ids = stringRedisTemplate.opsForSet().members(CHUNK_ID_SET);
        if (ids == null || ids.isEmpty()) {
            return 0L;
        }
        vectorStoreService.delete(new ArrayList<>(ids));
        stringRedisTemplate.delete(CHUNK_ID_SET);
        log.info("Cleared knowledge base, removed {} chunks", ids.size());
        return ids.size();
    }

    private String docChunkKey(String docId) {
        return DOC_CHUNK_SET_PREFIX + docId;
    }
}
