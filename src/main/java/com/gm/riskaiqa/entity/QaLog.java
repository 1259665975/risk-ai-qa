package com.gm.riskaiqa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答日志实体（MySQL qa_log 表）。
 * <p>记录每次 {@code /risk/qa} 请求的完整链路信息：问题、答案、
 * 召回文档、是否缓存、是否降级、耗时、客户端 IP 等。
 * 支持 MyBatis-Plus 逻辑删除。</p>
 */
@Data
@TableName("qa_log")
public class QaLog {

    /** 主键，自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 请求追踪 ID。 */
    private String traceId;

    /** 用户提问原文。 */
    private String question;

    /** 大模型回答文本。 */
    private String answer;

    /** 召回的知识片段（JSON 序列化）。 */
    private String referenceDocs;

    /** 召回匹配的文档片段数量。 */
    private Integer matchedCount;

    /** 1=命中缓存，0=未命中。 */
    private Integer hitCache;

    /** 1=降级回复，0=正常。 */
    private Integer degraded;

    /** 处理状态：OK / FAILED。 */
    private String status;

    /** 失败时的错误信息。 */
    private String errorMsg;

    /** 接口处理耗时（毫秒）。 */
    private Long costMs;

    /** 客户端 IP。 */
    private String clientIp;

    /** 逻辑删除标志（0=未删，1=已删）。 */
    @TableLogic
    private Integer deleted;

    /** 创建时间（由 {@code MetaObjectHandler} 自动填充）。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
