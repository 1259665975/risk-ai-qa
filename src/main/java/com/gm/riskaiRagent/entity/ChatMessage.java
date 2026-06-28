package com.gm.riskaiRagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private String question;
    private String answer;
    private String referenceDocs;
    private Integer fromCache;
    private Integer degraded;
    private Long costMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
