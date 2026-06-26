package com.gm.riskaiqa.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {

    private String oldPassword;
    private String newPassword;
}
