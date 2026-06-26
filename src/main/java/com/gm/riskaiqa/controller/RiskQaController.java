package com.gm.riskaiqa.controller;

import com.gm.riskaiqa.annotation.RateLimit;
import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.dto.QaRequest;
import com.gm.riskaiqa.dto.QaResponse;
import com.gm.riskaiqa.service.RagQaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 风控 RAG 智能问答 控制器。
 * <p>接收用户提问，触发向量检索 + LLM 调用流程，返回答案。
 * 接口受 {@link RateLimit @RateLimit} 保护。</p>
 */
@Tag(name = "RiskQA", description = "风控领域 RAG 智能问答")
@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskQaController {

    private final RagQaService ragQaService;

    @Operation(summary = "风控智能问答（RAG + 风控Prompt防幻觉 + Redis限流/缓存/降级）")
    @RateLimit(key = "risk-qa")
    @PostMapping("/qa")
    public Result<QaResponse> qa(@Valid @RequestBody QaRequest request) {
        return Result.success(ragQaService.ask(request));
    }
}
