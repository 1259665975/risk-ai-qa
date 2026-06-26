<<<<<<< HEAD
package com.gm.riskaiqa.config;

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

    /** Redis 答案缓存配置。 */
    @Data
    public static class Cache {
        /** 是否启用缓存。 */
        private boolean enabled = true;
        /** 缓存 TTL（分钟）。 */
        private long ttlMinutes = 60;
        /** 缓存 Key 前缀。 */
        private String keyPrefix = "risk-ai:qa:cache:";
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
        private String keyPrefix = "risk-ai:qa:rl:";
    }

    /** 服务降级配置。 */
    @Data
    public static class Degrade {
        /** LLM 不可用时的兜底回复文案。 */
        private String fallbackAnswer = "当前问答服务繁忙或暂时不可用，请稍后重试。";
    }
}
=======
package com.gm.riskaiqa.config;

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

    /** Redis 答案缓存配置。 */
    @Data
    public static class Cache {
        /** 是否启用缓存。 */
        private boolean enabled = true;
        /** 缓存 TTL（分钟）。 */
        private long ttlMinutes = 60;
        /** 缓存 Key 前缀。 */
        private String keyPrefix = "risk-ai:qa:cache:";
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
        private String keyPrefix = "risk-ai:qa:rl:";
    }

    /** 服务降级配置。 */
    @Data
    public static class Degrade {
        /** LLM 不可用时的兜底回复文案。 */
        private String fallbackAnswer = "当前问答服务繁忙或暂时不可用，请稍后重试。";
    }
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
