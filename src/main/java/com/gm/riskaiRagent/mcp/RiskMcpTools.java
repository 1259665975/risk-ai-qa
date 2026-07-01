package com.gm.riskaiRagent.mcp;

import com.gm.riskaiRagent.dto.DashboardStatsVO;
import com.gm.riskaiRagent.dto.RagentRequest;
import com.gm.riskaiRagent.dto.RagentResponse;
import com.gm.riskaiRagent.entity.SysCategory;
import com.gm.riskaiRagent.service.DashboardService;
import com.gm.riskaiRagent.service.EnhancedRetrievalService;
import com.gm.riskaiRagent.service.RagRagentService;
import com.gm.riskaiRagent.service.SysCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP 工具集：将风控 RAG 能力暴露给外部 MCP 客户端（Cursor / Claude Desktop 等）。
 * <p>通过 {@code @Tool} 注解声明，由 {@link com.gm.riskaiRagent.config.McpServerConfig} 注册到 MCP Server。</p>
 */
@Slf4j
@Service
public class RiskMcpTools {

    private final EnhancedRetrievalService enhancedRetrievalService;
    private final RagRagentService ragRagentService;
    private final DashboardService dashboardService;
    private final SysCategoryService sysCategoryService;

    public RiskMcpTools(EnhancedRetrievalService enhancedRetrievalService,
                        @Lazy RagRagentService ragRagentService,
                        DashboardService dashboardService,
                        SysCategoryService sysCategoryService) {
        this.enhancedRetrievalService = enhancedRetrievalService;
        this.ragRagentService = ragRagentService;
        this.dashboardService = dashboardService;
        this.sysCategoryService = sysCategoryService;
    }

    /**
     * 向量检索：从 Milvus 知识库召回与问题相关的文档片段（不调用大模型）。
     */
    @Tool(description = "在风控知识库中检索与问题相关的文档片段，返回原文摘录及来源文件名。"
            + "适用于需要先查看参考资料、或仅需检索不需生成答案的场景。")
    public String searchRiskKnowledge(
            @ToolParam(description = "检索关键词或自然语言问题，例如：员工年假规定、反欺诈规则") String query) {
        log.info("MCP tool searchRiskKnowledge: query={}", query);
        List<Document> docs = enhancedRetrievalService.retrieve(query, null, 5).getDocuments();
        if (docs == null || docs.isEmpty()) {
            return "未检索到相关风控文档片段。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("共检索到 ").append(docs.size()).append(" 条相关片段：\n\n");
        int i = 1;
        for (Document doc : docs) {
            Object source = doc.getMetadata().get("source");
            Object title = doc.getMetadata().get("title");
            sb.append("[").append(i++).append("] ");
            if (title != null) {
                sb.append("标题: ").append(title).append(" ");
            }
            if (source != null) {
                sb.append("来源: ").append(source);
            }
            if (doc.getScore() != null) {
                sb.append(" 相似度: ").append(String.format("%.3f", doc.getScore()));
            }
            sb.append("\n").append(truncate(doc.getText(), 600)).append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * 完整 RAG 问答：检索 + 风控 Prompt + 大模型生成答案。
     */
    @Tool(description = "基于风控知识库进行 RAG 智能问答。严格依据内部文档作答，文档无信息时回复"
            + "「暂无相关风控规则信息」，禁止编造。仅解答风控准入、审批、反欺诈、合规相关问题。")
    public String askRiskQuestion(
            @ToolParam(description = "用户的风控合规问题") String question) {
        log.info("MCP tool askRiskQuestion: question={}", question);
        RagentRequest request = new RagentRequest();
        request.setQuestion(question);
        request.setIncludeReferences(false);
        RagentResponse response = ragRagentService.ask(request);
        StringBuilder sb = new StringBuilder(response.getAnswer());
        if (response.isDegraded()) {
            sb.append("\n\n[系统提示：当前为降级回答，大模型服务可能不可用]");
        }
        if (response.isFromCache()) {
            sb.append("\n\n[缓存命中]");
        }
        sb.append("\n[traceId=").append(response.getTraceId())
                .append(", costMs=").append(response.getCostMs()).append("]");
        return sb.toString();
    }

    /**
     * 知识库运营统计。
     */
    @Tool(description = "获取风控知识库统计信息：文档总数、用户数、分类数、累计问答次数。")
    public String getKnowledgeBaseStats() {
        DashboardStatsVO stats = dashboardService.stats();
        return String.format(
                "知识库统计：文档 %d 篇，分类 %d 个，用户 %d 人，累计问答 %d 次。",
                stats.getDocuments(), stats.getCategories(), stats.getUsers(), stats.getRagentCount());
    }

    /**
     * 列出知识分类。
     */
    @Tool(description = "列出知识库中所有文档分类（ID、名称、文档数量），便于按分类限定检索范围。")
    public String listKnowledgeCategories() {
        List<SysCategory> categories = sysCategoryService.listAll();
        if (categories.isEmpty()) {
            return "当前暂无知识分类。";
        }
        return categories.stream()
                .map(c -> String.format("ID=%d, 名称=%s, 文档数=%d",
                        c.getId(), c.getName(), c.getDocumentCount() == null ? 0 : c.getDocumentCount()))
                .collect(Collectors.joining("\n"));
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
