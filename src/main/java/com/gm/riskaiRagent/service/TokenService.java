package com.gm.riskaiRagent.service;

import com.gm.riskaiRagent.entity.SysUser;
import com.gm.riskaiRagent.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String TOKEN_PREFIX = "risk-ai:auth:token:";
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;

    public String createToken(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                user.getId() + ":" + user.getRole(),
                TOKEN_TTL);
        return token;
    }

    public AuthUser resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String value = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            return null;
        }
        AuthUser authUser = new AuthUser();
        authUser.setId(Long.parseLong(parts[0]));
        authUser.setRole(parts[1]);
        return authUser;
    }
}
