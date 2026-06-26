package com.gm.riskaiqa.controller.api.user;

import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.dto.ChatRequest;
import com.gm.riskaiqa.dto.QaResponse;
import com.gm.riskaiqa.dto.SessionCreateRequest;
import com.gm.riskaiqa.entity.ChatSession;
import com.gm.riskaiqa.service.ChatSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "UserSessions")
@RestController
@RequestMapping("/api/user/sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final ChatSessionService chatSessionService;

    @GetMapping
    public Result<List<ChatSession>> list() {
        return Result.success(chatSessionService.listSessions());
    }

    @PostMapping
    public Result<ChatSession> create(@RequestBody(required = false) SessionCreateRequest request) {
        String title = request == null ? null : request.getTitle();
        return Result.success(chatSessionService.createSession(title));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chatSessionService.deleteSession(id);
        return Result.success();
    }

    @GetMapping("/{id}/messages")
    public Result<List<ChatSessionService.ChatMessageVO>> messages(@PathVariable Long id) {
        return Result.success(chatSessionService.listMessages(id));
    }
}
