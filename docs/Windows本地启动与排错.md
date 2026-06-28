# Windows 本地启动与排错指南

本文档汇总在 **Windows 10/11** 上启动 `risk-ai-ragent`（后端）+ `risk-ai-web`（前端）时遇到的典型问题与解决办法。

---

## 1. 标准启动顺序

按以下顺序操作，成功率最高：

```powershell
# ① 确保 Docker Desktop 已运行（状态为 Running）
docker ps

# ② 启动中间件（MySQL / Redis / Milvus）
cd f:\gm\risk-ai-ragent
docker compose up -d

# ③ 启动后端（JDK 17）
$env:JAVA_HOME="F:\tool\jdk-17.0.9"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:MYSQL_PORT="3307"
$env:MYSQL_PASSWORD="root"
$env:DASHSCOPE_API_KEY="你的百炼API密钥"
cd f:\gm\risk-ai-ragent
mvn spring-boot:run

# ④ 启动前端（新开一个 PowerShell 窗口）
cd f:\gm\risk-ai-web
npm run dev
```

| 服务 | 地址 | 默认账号 |
|------|------|----------|
| 前端 | http://localhost:5173 | admin / 123456 |
| 后端 API | http://localhost:8080 | 同上 |
| Swagger | http://localhost:8080/swagger-ui.html | — |

---

## 2. 环境与依赖清单

| 组件 | 版本要求 | 用途 |
|------|----------|------|
| JDK | **17** | 运行 Spring Boot |
| Maven | 3.8+ | 构建后端 |
| Node.js | 18+ | 运行前端 |
| Docker Desktop | 最新 | MySQL / Redis / Milvus |
| WSL2 | 2.x + Ubuntu | Docker Desktop 依赖（Windows） |

---

## 3. 问题汇总（按出现顺序）

### 3.1 Docker Desktop 报 WSL 错误

**现象**

- 弹窗：`There was a problem with WSL`
- 命令行：`failed to connect to dockerDesktopLinuxEngine`

**原因**

Docker Desktop 在 Windows 上默认依赖 **WSL2**，但 WSL 未安装或未就绪。

**解决步骤**

1. **管理员 PowerShell** 执行：
   ```powershell
   dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
   dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
   ```
2. 安装 WSL2 内核：https://aka.ms/wsl2kernel
3. **重启电脑**
4. 重启后执行：
   ```powershell
   wsl --update
   wsl --set-default-version 2
   ```
5. 安装 Ubuntu：
   - 推荐从 **微软商店** 安装「Ubuntu 22.04」（在线 `wsl --install -d Ubuntu` 可能因 GitHub 超时失败）
6. 再打开 Docker Desktop，等状态变为 **Running**

---

### 3.2 `wsl --install` 只显示帮助文档

**现象**

执行 `wsl --install` 后输出大量帮助文字，没有真正安装。

**原因**

旧版 WSL 不支持 `--install` 子命令。

**解决**

按 **3.1** 用手动 DISM 启用功能 + 商店安装 Ubuntu，不要依赖 `wsl --install`。

---

### 3.3 `wsl --install -d Ubuntu` 超时

**现象**

```
无法从 raw.githubusercontent.com 提取列表... WININET_E_TIMEOUT
```

**原因**

国内网络访问 GitHub 超时。

**解决**

从 **Microsoft Store** 搜索安装 **Ubuntu**，不走在线命令拉取。

---

### 3.4 Docker 拉镜像超时

**现象**

```
dial tcp registry-1.docker.io:443: connectex: ... failed to respond
```

**原因**

Docker Hub 在国内访问不稳定。

**解决**

使用国内镜像源手动拉取并打 tag，例如：

```powershell
docker pull docker.m.daocloud.io/library/mysql:8.0
docker tag docker.m.daocloud.io/library/mysql:8.0 mysql:8.0

docker pull docker.m.daocloud.io/library/redis:7.2
docker tag docker.m.daocloud.io/library/redis:7.2 redis:7.2

docker pull docker.m.daocloud.io/milvusdb/milvus:v2.3.9
docker tag docker.m.daocloud.io/milvusdb/milvus:v2.3.9 milvusdb/milvus:v2.3.9

docker pull docker.m.daocloud.io/minio/minio:RELEASE.2023-03-20T20-16-18Z
docker tag docker.m.daocloud.io/minio/minio:RELEASE.2023-03-20T20-16-18Z minio/minio:RELEASE.2023-03-20T20-16-18Z

docker pull quay.io/coreos/etcd:v3.5.5
```

然后执行 `docker compose up -d`。

---

### 3.5 MySQL 端口 3306 被占用

**现象**

```
bind: Only one usage of each socket address ... 3306
```

**原因**

本机已安装 MySQL，占用了 3306 端口。

**解决**

项目 `docker-compose.yml` 已将 Docker MySQL 映射到 **3307**：

```yaml
ports:
  - "3307:3306"
```

启动后端时必须指定：

```powershell
$env:MYSQL_PORT="3307"
$env:MYSQL_PASSWORD="root"
```

> `docker-compose` 里 root 密码是 `root`，不是 yml 默认的 `123456`。

---

### 3.6 后端启动报 `Unknown database 'risk_ai_ragent'`

**现象**

```
java.sql.SQLSyntaxErrorException: Unknown database 'risk_ai_ragent'
```

**原因**

- MySQL 没连上（端口/密码不对），或
- 数据库尚未创建

**解决**

1. 确认 Docker MySQL 在跑：`docker ps | findstr mysql`
2. 设置正确环境变量（见 3.5）
3. 重启后端；`schema.sql` 会在首次连接时自动建表

若用本机 MySQL（非 Docker），需手动建库：

```sql
CREATE DATABASE risk_ai_ragent DEFAULT CHARACTER SET utf8mb4;
```

---

### 3.7 文档上传报 HTTP 404

**现象**

前端提示：`HTTP 404 - No response body available`，Network 里请求 `upload` 失败。

**可能有两种原因，需区分：**

#### 情况 A：接口路径 404（较少见）

前端手动设置了 `Content-Type: multipart/form-data` 但缺少 **boundary**，Spring 无法识别 multipart 请求。

**已修复**：`risk-ai-web/src/api/admin.js` 中不再手动设置 Content-Type，由 axios 自动处理。

#### 情况 B：Embedding API 404（更常见）

接口其实已到达后端，失败在向量化阶段。Spring AI 把 URL 拼成了：

```
❌ https://dashscope.aliyuncs.com/compatible-mode/v1/v1/embeddings
✅ https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings
```

**已修复**：`application.yml` 中设置：

```yaml
spring.ai.openai:
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  chat:
    completions-path: /chat/completions   # 避免 /v1/v1/chat/completions
  embedding:
    embeddings-path: /embeddings          # 避免 /v1/v1/embeddings
```

**如何判断是哪种？**

看后端日志：若堆栈里有 `OpenAiApi.embeddings`，就是 **情况 B**（Embedding 404），不是上传接口本身 404。

---

### 3.8 聊天/问答也报 404

**原因**

与 3.7 情况 B 相同：`base-url` 已含 `/v1`，Spring AI 默认再拼 `/v1/chat/completions`。

**解决**

```yaml
chat:
  completions-path: /chat/completions
```

---

### 3.9 向量存储 vs 向量化混淆

| 组件 | 作用 | 配置项 |
|------|------|--------|
| **百炼 DashScope** | 把文本转成向量（Embedding） | `spring.ai.openai.embedding` |
| **百炼 DashScope** | 大模型生成回答（Chat） | `spring.ai.openai.chat` |
| **Milvus** | 存储向量、做相似度检索 | `spring.ai.vectorstore.milvus` |

上传文档流程：

```
文件 → 格式校验 → Tika 解析（Office/PDF/文本）或 百炼 OCR（图片）
     → 切片 → 百炼 Embedding → 写入 Milvus → MySQL 记元数据
```

支持格式与 OCR 模型选型见 [文档入库指南.md](文档入库指南.md)。

Milvus 不负责向量化，只负责存和搜。

---

### 3.10 API Key 配置

**当前方案**：聊天 + 向量化统一走阿里云百炼（千问）。

```yaml
spring.ai.openai:
  api-key: ${DASHSCOPE_API_KEY:...}
  base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
  chat:
    options:
      model: qwen-plus          # 千问对话模型
  embedding:
    options:
      model: text-embedding-v4  # 千问向量模型
```

**推荐**：不要把真实 Key 写进 yml 提交 Git，用环境变量：

```powershell
$env:DASHSCOPE_API_KEY="sk-xxx"
```

**可选千问模型**

| model | 说明 |
|-------|------|
| `qwen-turbo` | 快、便宜 |
| `qwen-plus` | 均衡（默认） |
| `qwen-max` | 效果最好、较贵 |
| `qwen-plus-latest` | 始终最新版 plus |

**图片 OCR 模型**（`risk-ai.document.vision-model` / `RISK_AI_VISION_MODEL`）

| model | 说明 |
|-------|------|
| `qwen-vl-plus` | 默认，通用视觉 + OCR |
| `qwen-vl-ocr-latest` | 专用 OCR，扫描件/表格推荐 |
| `vanchin/deepseek-ocr` | 百炼上的 DeepSeek OCR，可选 |

图片上传失败时，确认 Key 已开通对应视觉/OCR 模型。详见 [文档入库指南.md](文档入库指南.md)。

---

### 3.11 Milvus 维度不一致

**现象**

入库或检索时报维度相关错误。

**原因**

`text-embedding-v4` 默认 **1024 维**，必须与 Milvus 配置一致：

```yaml
embedding-dimension: 1024
```

若曾用 1536 维（OpenAI）建过 collection，需：

1. `DELETE /doc/clear` 或管理端清空文档
2. 改维度为 1024 并重启
3. 重新上传文档

---

### 3.12 管理端「创建时间 / 上传时间」列为空

**现象**

- 分类管理、文档管理表格中「创建时间」「上传时间」不显示
- 仪表盘「用户总数 / 文档总数 / 分类总数」显示为 `-`，仅「问答总数」有数字

**原因**

前后端字段名不一致：

| 页面 | 前端绑定 | 后端 JSON |
|------|----------|-----------|
| 分类 / 文档列表 | `createTime` | `createdAt` |
| 仪表盘统计 | `userCount` / `documentCount` / `categoryCount` | `users` / `documents` / `categories` |

**解决**

已在 `risk-ai-web` 的 `src/api/admin.js` 与 `src/utils/dateTime.js` 中做字段映射（`createdAt` → `createTime`、统计字段别名）。拉取最新前端代码后刷新浏览器（必要时 Ctrl+F5）。

---

### 3.13 图片 OCR 报 `invalid header value: "Bearer 你的key"`

**现象**

管理端上传 PNG/JPG 时提示：

```
图片识别失败 [xxx.png]: invalid header value: "Bearer 你的key"
```

**原因**

`application-local.yml` 或环境变量里仍是占位符 `你的key`，不是真实百炼 API Key。

**解决**

1. 打开 [百炼控制台](https://bailian.console.aliyun.com/) 复制 API Key（一般以 `sk-` 开头）
2. 写入 `src/main/resources/application-local.yml`（该文件已在 `.gitignore`，勿提交）：

   ```yaml
   spring:
     ai:
       openai:
         api-key: sk-你的真实密钥
   ```

   或启动前设置：`$env:DASHSCOPE_API_KEY="sk-xxx"`

3. **必须重启后端**（改 yml 后热更新不生效）

**注意**

- 不要使用 `sk-ws-...` 等非百炼格式 Key，否则会 401
- TXT/Word 入库可能正常，但**图片必须额外调用视觉 OCR**，Key 无效时只有图片会失败

---

### 3.14 用户端没有上传入口，如何检索图片内容？

**现象**

普通用户登录后只有「智能问答」，找不到上传图片按钮，怀疑不支持图片检索。

**说明（设计如此）**

| 角色 | 入口 | 能力 |
|------|------|------|
| **管理员** `/admin/documents` | 文档管理 | 上传 TXT/PDF/Word/**图片** 等并入库 |
| **普通用户** `/user/chat` | 智能问答 | 仅**文字提问**，从已入库知识中检索 |

图片检索流程：

```
管理员上传图片 → 百炼 OCR 成文字 → 切片向量化 → Milvus
用户文字提问 → 向量检索命中 OCR 文本 → 大模型生成回答
```

- **不是「以图搜图」**，而是「图片先变文字，再按文字检索」
- 用户端**不需要**也不提供上传功能；图片须由管理员先入知识库

**正确使用**

1. 用 `admin` 在管理端上传图片并选择分类，确认状态为「已入库」
2. 用 `user1` 在用户端问答，**选择对应分类**（与上传时一致）
3. 提问尽量包含图片里的关键词（如报告标题、专有名词）

详见 [文档入库指南.md](文档入库指南.md) 第 7 节。

---

### 3.15 问答返回「当前问答服务繁忙或暂时不可用」

**现象**

用户端或管理端问答时，回答为固定文案「当前问答服务繁忙或暂时不可用，请稍后重试。」

**原因**

这是 **大模型调用失败后的降级回答**（`degraded=true`），常见原因：

- API Key 无效、过期或未开通 `qwen-plus` 等对话模型
- 百炼接口超时或限流
- 网络问题

**与「检索不到图片」的区别**

| 情况 | 典型回答 | `degraded` |
|------|----------|------------|
| 检索无命中 | 「暂无相关风控规则信息」 | `false` |
| 大模型失败 | 「当前问答服务繁忙…」 | `true` |

**解决**

1. 检查 `DASHSCOPE_API_KEY` / `application-local.yml`
2. 查看后端日志中 `LLM invocation failed, degraded`
3. 修复 Key 后重启后端，重新提问（勿依赖旧缓存答案）

---

### 3.16 修改 API Key 后未生效

**现象**

已改 `application-local.yml` 中的 Key，但 OCR 或问答仍报旧错误。

**原因**

Spring Boot 启动时加载配置，**运行中修改 yml 不会自动生效**。

**解决**

停止 `mvn spring-boot:run`（或结束占用 8080 的进程）后重新启动。若同时设置了环境变量 `DASHSCOPE_API_KEY`，其优先级高于 `application-local.yml`。

---

## 4. 修改配置后如何重启

### 只改了 `application.yml`

```powershell
# 找到并停止 8080 进程，或 Ctrl+C 停掉 mvn spring-boot:run
$env:JAVA_HOME="F:\tool\jdk-17.0.9"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:MYSQL_PORT="3307"
$env:MYSQL_PASSWORD="root"
cd f:\gm\risk-ai-ragent
mvn spring-boot:run
```

### 只改了前端代码

Vite 一般自动热更新；不行就 `Ctrl+C` 后 `npm run dev`。

### 改了 `docker-compose.yml`

```powershell
cd f:\gm\risk-ai-ragent
docker compose down
docker compose up -d
```

---

## 5. 快速自检命令

```powershell
# WSL 是否正常
wsl -l -v

# Docker 是否运行
docker ps

# 中间件是否都在
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 后端是否响应
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"username":"admin","password":"123456"}'

# 前端是否运行
# 浏览器打开 http://localhost:5173
```

**期望的 Docker 容器**

| 容器名 | 端口 |
|--------|------|
| risk-ai-mysql | 3307→3306 |
| risk-ai-redis | 6379 |
| risk-ai-milvus | 19530, 9091 |
| risk-ai-etcd | 内部 |
| risk-ai-minio | 内部 |

---

## 6. 架构一图流

```
┌─────────────┐     /api/*      ┌──────────────────┐
│ risk-ai-web │ ──────────────► │  risk-ai-ragent      │
│  :5173      │                 │  :8080           │
└─────────────┘                 └────────┬─────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              │                          │                          │
              ▼                          ▼                          ▼
        ┌──────────┐            ┌──────────────┐            ┌─────────────┐
        │  MySQL   │            │    Redis     │            │   Milvus    │
        │  :3307   │            │    :6379     │            │   :19530    │
        └──────────┘            └──────────────┘            └─────────────┘
                                         │
              ┌──────────────────────────┴──────────────────────────┐
              │                    百炼 DashScope                      │
              │   Chat: qwen-plus          Embedding: text-embedding-v4 │
              └─────────────────────────────────────────────────────────┘
```

---

## 7. 相关文档

- [README.md](../README.md) — 项目说明与 API 示例
- [文档入库指南.md](文档入库指南.md) — 多格式上传与图片 OCR
- [快速理解代码.md](快速理解代码.md) — 代码阅读指南
- [application.yml](../src/main/resources/application.yml) — 全部配置项
