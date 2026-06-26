package com.gm.riskaiqa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PortalQaRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    private List<Long> categoryIds;

    private boolean includeReferences = true;
}
