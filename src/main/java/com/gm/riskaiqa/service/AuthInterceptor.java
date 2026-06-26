package com.gm.riskaiqa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.common.ResultCode;
import com.gm.riskaiqa.entity.SysUser;
import com.gm.riskaiqa.security.AuthContext;
import com.gm.riskaiqa.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        AuthUser authUser = tokenService.resolve(token);
        if (authUser == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    Result.error(ResultCode.UNAUTHORIZED.getCode(), "未登录或登录已过期"));
            return false;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin") && !authUser.isAdmin()) {
            boolean readOnlyCategory = "GET".equalsIgnoreCase(request.getMethod())
                    && "/api/admin/categories".equals(uri);
            if (!readOnlyCategory) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        Result.error(ResultCode.FORBIDDEN.getCode(), "无权限访问"));
                return false;
            }
        }

        AuthContext.set(authUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
