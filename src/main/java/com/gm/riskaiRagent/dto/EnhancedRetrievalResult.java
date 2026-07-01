package com.gm.riskaiRagent.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.List;

/**
 * 增强检索结果（混合检索 + Rerank）。
 */
@Data
@Builder
public class EnhancedRetrievalResult {

    @Builder.Default
    private List<Document> documents = Collections.emptyList();

    private boolean hybridUsed;

    private boolean rerankUsed;
}
