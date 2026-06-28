package com.gm.riskaiRagent.dto;

import lombok.Data;

@Data
public class UserSaveRequest {

    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;
}
