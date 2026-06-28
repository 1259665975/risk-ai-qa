package com.gm.riskaiRagent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Redis 固定窗口限流器。
 * <p>核心逻辑由一段 Lua 脚本（原子性 INCR + 首次命中 EXPIRE）保证并发安全。
 * 当 Redis 不可用时会 fail-open，确保不阻塞业务流量。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    /**
     * 固定窗口限流 Lua 脚本。
     * <ul>
     *   <li>KEYS[1]：限流 Key（如 "risk-ai:ragent:rl:risk-ragent:127.0.0.1"）</li>
     *   <li>ARGV[1]：窗口时长（秒）</li>
     * </ul>
     * 首次 INCR 时同时设置 EXPIRE，确保窗口自动过期。
     */
    private static final RedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('incr', KEYS[1]) "
                    + "if tonumber(current) == 1 then "
                    + "  redis.call('expire', KEYS[1], ARGV[1]) "
                    + "end "
                    + "return current",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取一次许可。
     *
     * @param key           限流 Key
     * @param maxRequests   窗口内最大请求数
     * @param windowSeconds 窗口时长（秒）
     * @return {@code true} 允许通过，{@code false} 拒绝
     */
    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        try {
            Long count = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds));
            return count != null && count <= maxRequests;
        } catch (Exception e) {
            // Redis 不可用时放行，防止限流击穿导致业务不可用。
            log.warn("Rate limiter unavailable, allowing request. key={}", key, e);
            return true;
        }
    }
}