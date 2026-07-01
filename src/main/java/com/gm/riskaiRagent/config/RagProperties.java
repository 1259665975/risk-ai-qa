package com.gm.riskaiRagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用所有可调参数集中绑定类。
 * <p>前缀 {@code risk-ai}，对应 {@code application.yml} 中 {@code risk-ai.*} 子树。
 * 所有参数均有默认值，通过环境变量或 yml 覆盖。</p>
 */
@Data
@ConfigurationProperties(prefix = "risk-ai")
public class RagProperties {

    private Chunk chunk = new Chunk();
    private Rag rag = new Rag();
    private Retrieval retrieval = new Retrieval();
    private MultiHop multiHop = new MultiHop();
    private Document document = new Document();
    private Cache cache = new Cache();
    private RateLimit rateLimit = new RateLimit();
    private Degrade degrade = new Degrade();

    /** 文本切片规则（基于 Token）。 */
    @Data
    public static class Chunk {
        /** 每个切片的 Token 数上限。 */
        private int size = 800;
        /** 相邻切片的重叠 Token 数。 */
        private int overlap = 150;
        /** jtokkit 编码名称：CL100K_BASE / O200K_BASE / R50K_BASE。 */
        private String encoding = "CL100K_BASE";
    }

    /** 向量检索参数。 */
    @Data
    public static class Rag {
        /** 检索返回的最相似文档数。 */
        private int topK = 5;
        /** 相似度阈值，低于此值的文档被过滤。 */
        private double similarityThreshold = 0.5;
    }

    /** 混合检索与 Rerank 精排配置。 */
    @Data
    public static class Retrieval {
        private Hybrid hybrid = new Hybrid();
        private Rerank rerank = new Rerank();
    }

    /** 向量 + BM25 混合召回（RRF 融合）。 */
    @Data
    public static class Hybrid {
        /** 是否启用混合检索。 */
        private boolean enabled = true;
        /** 向量/关键词各自召回候选数，融合后再精排。 */
        private int candidateTopK = 20;
        /** RRF 平滑常数 k。 */
        private int rrfK = 60;
    }

    /** 百炼 qwen3-rerank 精排。 */
    @Data
    public static class Rerank {
        /** 是否启用 Rerank。 */
        private boolean enabled = true;
        /** Rerank 模型名。 */
        private String model = "qwen3-rerank";
        /** Rerank API 地址（OpenAI 兼容 reranks 端点）。 */
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-api/v1/reranks";
        /** 排序任务提示，面向问答检索场景。 */
        private String instruct = "Given a web search query, retrieve relevant passages that answer the query.";
        /** API 失败时是否回退到融合排序结果。 */
        private boolean failOpen = true;
    }

    /**
     * 多跳检索（Multi-hop Retrieval）配置。
     * <p>默认 {@code enabled=false}，与原有单跳行为完全一致。</p>
     */
    @Data
    public static class MultiHop {
        /** 是否启用多跳检索。 */
        private boolean enabled = false;
        /** 最大跳数（含第 1 跳原始问题检索）。 */
        private int maxHops = 2;
        /** 每一跳（第 2 跳起）最多生成的子查询数。 */
        private int subQueriesPerHop = 2;
        /** 每一跳向量检索的 topK（通常小于最终合并数）。 */
        private int hopTopK = 3;
        /** 合并去重后保留的最大文档数（默认与 rag.topK 一致时可不配）。 */
        private int finalTopK = 5;
        /** 是否用大模型根据上一跳结果生成子查询；false 时使用规则模板扩展。 */
        private boolean useLlmSubQuery = true;
    }

    /** Redis 答案缓存配置。 */
    @Data
    public static class Cache {
        /** 是否启用缓存。 */
        private boolean enabled = true;
        /** 缓存 TTL（分钟）。 */
        private long ttlMinutes = 60;
        /** 缓存 Key 前缀。 */
        private String keyPrefix = "risk-ai:ragent:cache:";
    }

    /** Redis 固定窗口限流配置。 */
    @Data
    public static class RateLimit {
        /** 是否启用限流。 */
        private boolean enabled = true;
        /** 窗口内允许的最大请求次数。 */
        private int maxRequests = 20;
        /** 统计窗口时长（秒）。 */
        private int windowSeconds = 60;
        /** 限流 Key 前缀。 */
        private String keyPrefix = "risk-ai:ragent:rl:";
    }

    /** 服务降级配置。 */
    @Data
    public static class Degrade {
        /** LLM 不可用时的兜底回复文案。 */
        private String fallbackAnswer = "当前问答服务繁忙或暂时不可用，请稍后重试。";
    }

    /** 文档入库与图片 OCR 配置。 */
    @Data
    public static class Document {
        /** 百炼视觉模型，用于图片文字识别。 */
        private String visionModel = "qwen-vl-plus";
        /** 图片 OCR 提示词。 */
        private String ocrPrompt = """
                你是金融风控文档 OCR 助手。请完整提取图片中的全部可见文字（含表格、标题、页眉页脚），\
                保持原有层次与条目结构；若有印章、签名、图表说明也请文字化描述。\
                只输出识别内容，不要添加解释或总结。若图片中无可读文字，仅回复「无文字内容」。""";
    }
}
