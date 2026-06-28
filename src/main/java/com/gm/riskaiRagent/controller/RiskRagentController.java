package com.gm.riskaiRagent.controller;

import com.gm.riskaiRagent.annotation.RateLimit;
import com.gm.riskaiRagent.common.Result;
import com.gm.riskaiRagent.dto.RagentRequest;
import com.gm.riskaiRagent.dto.RagentResponse;
import com.gm.riskaiRagent.service.RagRagentService;
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
@Tag(name = "RiskRagent", description = "风控领域 RAG 智能 RAgent")
@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskRagentController {

    private final RagRagentService ragRagentService;

    @Operation(summary = "风控智能 RAgent（RAG + 风控Prompt防幻觉 + Redis限流/缓存/降级）")
    @RateLimit(key = "risk-ragent")
    @PostMapping("/ragent")
    public Result<RagentResponse> ragent(@Valid @RequestBody RagentRequest request) {
        return Result.success(ragRagentService.ask(request));
    }
}
