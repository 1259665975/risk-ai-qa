package com.gm.riskaiqa.controller.api.admin;

import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.dto.PortalQaRequest;
import com.gm.riskaiqa.dto.QaRequest;
import com.gm.riskaiqa.dto.QaResponse;
import com.gm.riskaiqa.service.RagQaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminQa")
@RestController
@RequestMapping("/api/admin/qa")
@RequiredArgsConstructor
public class AdminQaController {

    private final RagQaService ragQaService;

    @PostMapping("/ask")
    public Result<QaResponse> ask(@Valid @RequestBody PortalQaRequest request) {
        QaRequest qaRequest = new QaRequest();
        qaRequest.setQuestion(request.getQuestion());
        qaRequest.setIncludeReferences(request.isIncludeReferences());
        return Result.success(ragQaService.ask(qaRequest, request.getCategoryIds()));
    }
}
