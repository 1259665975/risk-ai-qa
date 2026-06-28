package com.gm.riskaiRagent.config;

import com.gm.riskaiRagent.mcp.RiskMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * MCP Server 配置：将 {@link RiskMcpTools} 注册为 MCP 工具。
 * <p>配合 {@code spring-ai-starter-mcp-server-webmvc}，通过 SSE 对外暴露：
 * <ul>
 *   <li>SSE 连接：{@code GET /sse}</li>
 *   <li>消息端点：{@code POST /mcp/message}</li>
 * </ul>
 * MCP 客户端（Cursor / Claude Desktop 等）连接后即可调用风控 RAG 工具。</p>
 */
@Configuration
public class McpServerConfig {

    @Bean
    @Lazy
    public ToolCallbackProvider riskMcpToolCallbackProvider(RiskMcpTools riskMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(riskMcpTools)
                .build();
    }
}
