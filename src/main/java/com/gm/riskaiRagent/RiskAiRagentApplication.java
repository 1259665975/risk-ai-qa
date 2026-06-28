package com.gm.riskaiRagent;

import com.gm.riskaiRagent.config.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 风控智能 RAgent 启动入口。
 */
@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class RiskAiRagentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskAiRagentApplication.class, args);
    }
}
