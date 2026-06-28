package com.gm.riskaiRagent.aspect;

import com.gm.riskaiRagent.annotation.RateLimit;
import com.gm.riskaiRagent.common.BusinessException;
import com.gm.riskaiRagent.common.ResultCode;
import com.gm.riskaiRagent.config.RagProperties;
import com.gm.riskaiRagent.service.RateLimitService;
import com.gm.riskaiRagent.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Module 3 - applies Redis fixed-window rate limiting to {@link RateLimit}-annotated methods.
 * Limit is enforced per client IP. Exceeding the quota triggers a 429-style degradation.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RagProperties ragProperties;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RagProperties.RateLimit cfg = ragProperties.getRateLimit();
        if (!cfg.isEnabled()) {
            return joinPoint.proceed();
        }

        int maxRequests = rateLimit.maxRequests() > 0 ? rateLimit.maxRequests() : cfg.getMaxRequests();
        int windowSeconds = rateLimit.windowSeconds() > 0 ? rateLimit.windowSeconds() : cfg.getWindowSeconds();

        String name = rateLimit.key().isBlank()
                ? ((MethodSignature) joinPoint.getSignature()).getMethod().getName()
                : rateLimit.key();
        String redisKey = cfg.getKeyPrefix() + name + ":" + WebUtil.clientIp();

        boolean allowed = rateLimitService.tryAcquire(redisKey, maxRequests, windowSeconds);
        if (!allowed) {
            log.warn("Rate limit exceeded: key={}, max={}/{}s", redisKey, maxRequests, windowSeconds);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }
        return joinPoint.proceed();
    }
}
