package com.gm.riskaiRagent.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.List;

/**
 * 多跳检索结果（内部使用）。
 */
@Data
@Builder
public class MultiHopRetrievalResult {

    /** 合并去重后的文档列表。 */
    @Builder.Default
    private List<Document> documents = Collections.emptyList();

    /** 实际执行的检索跳数（单跳时为 1）。 */
    private int hops;

    /** 是否启用了多跳检索。 */
    private boolean multiHopUsed;

    /** 各跳生成的子查询（便于日志排查）。 */
    @Builder.Default
    private List<String> subQueries = Collections.emptyList();
}
