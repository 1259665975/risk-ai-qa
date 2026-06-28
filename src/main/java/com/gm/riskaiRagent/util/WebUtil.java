package com.gm.riskaiRagent.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Web request helpers.
 */
public final class WebUtil {

    private static final String UNKNOWN = "unknown";

    private WebUtil() {
    }

    public static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /**
     * Resolves the real client IP, honoring common reverse-proxy headers.
     */
    public static String clientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return UNKNOWN;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isValid(ip)) {
            // X-Forwarded-For may contain a chain: client, proxy1, proxy2
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (isValid(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private static boolean isValid(String ip) {
        return ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
