package com.gm.riskaiqa;

import com.gm.riskaiqa.config.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 风控智能问答系统启动入口。
 * <p>Spring Boot 应用主类，自动扫描 {@code com.gm.riskaiqa} 包下的组件。
 * 注册 {@link RagProperties} 配置属性绑定。</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class RiskAiQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskAiQaApplication.class, args);
    }
}
