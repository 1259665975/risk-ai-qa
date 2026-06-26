<<<<<<< HEAD
package com.gm.riskaiqa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置。
 * <p>允许前端页面从 localhost / 127.0.0.1（任意端口、http/https）发起的所有请求跨域访问。
 * 使用 {@link CorsFilter} 在过滤器链最前端统一处理，覆盖全部接口路径。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带 Cookie / 凭证
        config.setAllowCredentials(true);
        // 允许的来源：localhost / 127.0.0.1 任意端口（携带凭证时不能用 "*"，须用 originPattern）
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOriginPattern("https://127.0.0.1:*");
        // 允许所有请求头与请求方法
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 暴露所有响应头，预检结果缓存 1 小时
        config.addExposedHeader("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
=======
package com.gm.riskaiqa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置。
 * <p>允许前端页面从 localhost / 127.0.0.1（任意端口、http/https）发起的所有请求跨域访问。
 * 使用 {@link CorsFilter} 在过滤器链最前端统一处理，覆盖全部接口路径。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带 Cookie / 凭证
        config.setAllowCredentials(true);
        // 允许的来源：localhost / 127.0.0.1 任意端口（携带凭证时不能用 "*"，须用 originPattern）
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOriginPattern("https://127.0.0.1:*");
        // 允许所有请求头与请求方法
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 暴露所有响应头，预检结果缓存 1 小时
        config.addExposedHeader("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
