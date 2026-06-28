package com.gm.riskaiRagent.controller.api.user;

import com.gm.riskaiRagent.common.Result;
import com.gm.riskaiRagent.dto.ChatRequest;
import com.gm.riskaiRagent.dto.RagentResponse;
import com.gm.riskaiRagent.service.ChatSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UserChat")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserChatController {

    private final ChatSessionService chatSessionService;

    @PostMapping("/chat")
    public Result<RagentResponse> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(chatSessionService.chat(request));
    }
}
