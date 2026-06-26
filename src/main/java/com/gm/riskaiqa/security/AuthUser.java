package com.gm.riskaiqa.security;

import com.gm.riskaiqa.entity.SysUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUser {

    private Long id;
    private String username;
    private String role;

    public static AuthUser from(SysUser user) {
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setUsername(user.getUsername());
        authUser.setRole(user.getRole());
        return authUser;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
