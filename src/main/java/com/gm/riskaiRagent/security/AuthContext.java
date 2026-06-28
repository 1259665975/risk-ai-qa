package com.gm.riskaiRagent.security;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        AuthUser user = get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
