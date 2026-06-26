<<<<<<< HEAD
package com.gm.riskaiqa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置。
 * <p>为所有暴露的 API 生成标准的 OpenAPI 3.0 文档，可通过
 * {@code /swagger-ui.html} 或 {@code /v3/api-docs} 访问。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI riskAiQaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Risk AI Q&A API")
                .description("RAG-based intelligent Q&A for the risk domain (Spring AI + Milvus + Redis + MySQL)")
                .version("v1.0.0"));
    }
}
=======
package com.gm.riskaiqa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置。
 * <p>为所有暴露的 API 生成标准的 OpenAPI 3.0 文档，可通过
 * {@code /swagger-ui.html} 或 {@code /v3/api-docs} 访问。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI riskAiQaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Risk AI Q&A API")
                .description("RAG-based intelligent Q&A for the risk domain (Spring AI + Milvus + Redis + MySQL)")
                .version("v1.0.0"));
    }
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
