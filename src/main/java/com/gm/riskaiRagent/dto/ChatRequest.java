package com.gm.riskaiRagent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    private Long sessionId;

    @jakarta.validation.constraints.NotBlank(message = "question 不能为空")
    private String question;

    private List<Long> categoryIds;

    private boolean includeReferences = true;
}
