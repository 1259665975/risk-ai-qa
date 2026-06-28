package com.gm.riskaiRagent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * {@code POST /risk/ragent} 请求体 DTO。
 * <p>包含用户提出的问题 {@code question}，以及是否返回引用片段的标志。</p>
 */
@Data
public class RagentRequest {

    /** 用户提问文本，不能为空、不超过 2000 字符。 */
    @NotBlank(message = "question 不能为空")
    @Size(max = 2000, message = "question 长度不能超过 2000")
    private String question;

    /** 是否在响应中返回检索到的参考文档片段（默认返回）。 */
    private boolean includeReferences = true;
}
