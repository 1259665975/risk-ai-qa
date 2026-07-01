package com.gm.riskaiRagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * {@code POST /risk/ragent} 响应体 DTO。
 * <p>包含 {@code traceId}（追踪标识）、大模型回答、召回文档引用（可选）、
 * 是否命中缓存、是否触发降级、以及接口耗时。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagentResponse implements Serializable {

    /** 请求追踪 ID，全局唯一，可用于日志检索。 */
    private String traceId;
    /** 大模型生成的回答文本。 */
    private String answer;
    /** 检索到的知识片段列表。 */
    private List<ReferenceChunk> references;
    /** 是否命中 Redis 缓存。 */
    private boolean fromCache;
    /** 是否启用降级回复（大模型不可用时）。 */
    private boolean degraded;
    /** 接口处理耗时（毫秒）。 */
    private long costMs;
    /** 是否使用了多跳检索（默认 false，与改造前一致）。 */
    private boolean multiHopUsed;
    /** 实际检索跳数（单跳为 1）。 */
    private int retrievalHops;
    /** 是否使用了混合检索（向量 + 关键词 RRF）。 */
    private boolean hybridRetrievalUsed;
    /** 是否使用了 Rerank 精排。 */
    private boolean rerankUsed;
}
