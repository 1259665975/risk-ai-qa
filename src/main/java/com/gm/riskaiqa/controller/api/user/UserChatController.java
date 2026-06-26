package com.gm.riskaiqa.controller.api.user;

import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.dto.ChatRequest;
import com.gm.riskaiqa.dto.QaResponse;
import com.gm.riskaiqa.service.ChatSessionService;
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
    public Result<QaResponse> chat(@Valid @RequestBody ChatRequest request) {
        return Result.success(chatSessionService.chat(request));
    }
}
