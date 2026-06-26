# Risk AI Q&A

风险领域智能问答系统，基于 **RAG（检索增强生成）**。支持上传 TXT/PDF 文档解析入库，按 **800 Token 切片、150 Token 重叠** 向量化存入 Milvus；问答时检索相关知识、在**风控 Prompt 约束下**调用大模型生成答案（禁止幻觉），并提供 **Redis 限流 + 缓存 + 服务降级** 与 **MySQL 问答日志**。

> 需求文档 `risk-ai-qa.md` 为空，本项目按目录名 + 你给出的功能清单实现。

**新人上手**：如何快速理解代码 → [docs/快速理解代码.md](docs/快速理解代码.md)

**Windows 启动排错**：本地启动常见问题 → [docs/Windows本地启动与排错.md](docs/Windows本地启动与排错.md)

## 技术栈

| 组件 | 选型 | 说明 |
| --- | --- | --- |
| JDK | **Java 17** | |
| 框架 | **Spring Boot 3.4.5** | 见"版本说明" |
| AI | **Spring AI 1.0.0** | OpenAI 兼容 Chat + Embedding |
| 向量库 | **Milvus 2.3.x** | 知识向量存储/检索 |
| 缓存/限流 | **Redis 7.x** | 答案缓存、IP 限流、降级 |
| 关系库 | **MySQL 8.x** | 问答日志 |
| ORM | **MyBatis-Plus 3.5.16** | Mapper / 分页 / 逻辑删除 |
| 文档解析 | **Apache Tika 3.2.3** | TXT/PDF 等统一解析 |
| 分词计数 | **jtokkit 1.1.0** | 800/150 Token 切片 |
| 构建 | **Maven** | |
| 文档 | springdoc-openapi (Swagger UI) | |

### 版本说明（重要）

需求写的是 **Spring Boot 3.2 + Spring AI**，但二者不可兼容：Spring AI `1.0.x` GA **要求 Boot 3.4+**；而能配 Boot 3.2 的 Spring AI `0.8.x` 仅在 Spring 官方 milestone 仓库，本机 Maven 强制走 aliyun 镜像（`mirrorOf=*`）**下载不到**（实测 aliyun 最低只有 `1.0.0-M5`）。因此采用当前环境可稳定构建的最接近组合：**Spring Boot 3.4.5 + Spring AI 1.0.0 GA**。

## 四大模块

1. **知识库文档解析入库**：Tika 解析 TXT/PDF → jtokkit 800/150 Token 切片 → Embedding → Milvus，chunk id 记录到 Redis 便于清空。
2. **RAG 智能问答**：向量检索召回上下文 → 风控 Prompt 严格约束（仅依据上下文、禁止编造、无信息则明确告知）→ Spring AI 调用大模型。
3. **Redis 限流 + 缓存 + 服务降级**：基于注解 `@RateLimit` + AOP + Lua 固定窗口按 IP 限流；问题答案缓存（MD5 key + TTL）；大模型异常时返回降级兜底答案。
4. **MySQL 问答日志**：MyBatis-Plus 记录每次问答的 traceId、问题、答案、命中缓存、是否降级、耗时、来源 IP 等。

## 目录结构

```
src/main/java/com/gm/riskaiqa
├── RiskAiQaApplication.java
├── annotation/      @RateLimit 限流注解
├── aspect/          RateLimitAspect 限流切面（模块3）
├── common/          Result / ResultCode / BusinessException / 全局异常处理器
├── config/          RagProperties(全部可调参数) / Web / OpenApi / Redis / MybatisPlus / 字段自动填充
├── controller/      DocController(/doc/*) / RiskQaController(/risk/qa)
├── dto/             QaRequest / QaResponse / ReferenceChunk / IngestResponse
├── entity/          QaLog（MySQL 问答日志，模块4）
├── mapper/          QaLogMapper（MyBatis-Plus）
├── service/         DocumentService(模块1) / RagQaService(模块2) / RateLimitService+缓存降级(模块3) / QaLogService(模块4) / VectorStoreService
└── util/            DocumentParser(Tika) / TokenTextChunker(800/150) / WebUtil(取IP)
src/main/resources
├── application.yml      主配置（所有参数 + 环境变量占位，无硬编码密钥）
├── application-dev.yml
└── schema.sql           qa_log 建表
docker-compose.yml       MySQL/Redis/Milvus(+etcd/minio)
```

## 快速开始

### 1. 启动中间件

```bash
docker compose up -d
```

### 2. 配置大模型（不硬编码，走环境变量）

```powershell
# 必填：百炼 API Key（勿写入 yml 或提交 Git）
$env:DASHSCOPE_API_KEY="your-dashscope-api-key"

# 可选
$env:OPENAI_CHAT_MODEL="qwen-plus"
$env:OPENAI_EMBEDDING_MODEL="text-embedding-v4"
$env:MYSQL_PORT="3307"
$env:MYSQL_PASSWORD="root"
```

也可复制 `.env.example` 为 `.env` 填入密钥，启动前在 PowerShell 中加载环境变量。  
其余 `MYSQL_* / REDIS_* / MILVUS_*` 见 `application.yml`，均有默认值。

### 3. 运行（需 JDK 17）

```bash
mvn spring-boot:run
# 或
mvn -DskipTests package
java -jar target/risk-ai-qa.jar
```

Swagger UI：<http://localhost:8080/swagger-ui.html>

## 三个接口

统一返回：`{ "code": 200, "message": "success", "data": ... }`

### 1) 文档入库 `POST /doc/ingest`

```bash
curl -X POST http://localhost:8080/doc/ingest -F "file=@risk_policy.pdf"
```

返回：`fileName / docId / charCount / tokenCount / chunkCount`

### 2) 风控问答 `POST /risk/qa`

```bash
curl -X POST http://localhost:8080/risk/qa \
  -H "Content-Type: application/json" \
  -d '{"question":"信用风险的常见缓释手段有哪些？","includeReferences":true}'
```

返回：`traceId / answer / references[] / fromCache / degraded / costMs`

- 命中缓存：`fromCache=true`；
- 大模型不可用：`degraded=true` 且返回兜底答案；
- 超过 IP 限流（默认 20 次/60s）：返回 `code=429`。

### 3) 清空知识库 `DELETE /doc/clear`

```bash
curl -X DELETE http://localhost:8080/doc/clear
```

返回：`{ "removedChunks": N }`

## 防幻觉策略

`RagQaService` 中的风控系统 Prompt 严格要求：**仅依据检索到的知识库上下文作答；上下文不足时必须回复"知识库中未找到相关信息"，禁止编造**；`temperature` 默认 0.1，进一步降低发散。

## 关键可调参数（application.yml → `risk-ai.*`）

| 配置 | 默认 | 说明 |
| --- | --- | --- |
| `chunk.size` / `chunk.overlap` | 800 / 150 | Token 切片与重叠 |
| `rag.top-k` / `rag.similarity-threshold` | 5 / 0.5 | 检索召回 |
| `cache.ttl-minutes` | 60 | 答案缓存时长 |
| `rate-limit.max-requests` / `window-seconds` | 20 / 60 | 限流阈值 |
| `degrade.fallback-answer` | … | 降级兜底文案 |

## 备注

- 本机默认 `java` 为 1.8，构建/运行请使用 JDK 17（如 `F:\tool\jdk-17.0.9`）。
- 启动时 `schema.sql` 自动建 `qa_log` 表；Milvus collection（默认 `risk_knowledge`）在 `initialize-schema=true` 时自动创建。
