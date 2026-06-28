package com.gm.riskaiRagent.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {

    private String oldPassword;
    private String newPassword;
}
