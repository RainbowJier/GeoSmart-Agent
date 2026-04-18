# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指导。

## 项目概述

GeoSmart-Agent 是面向国土空间规划的全栈智能助手。通过 RAG（检索增强生成）实现政策咨询，通过 Agent（智能体工具调用）实现业务办理。旨在解决国土法规更新快、查询难，以及业务系统操作门槛高的问题。

后端基于 Spring Boot 3.5 + LangChain4j 1.13，前端基于 Vue 3 + Vite 8。知识库检索采用向量数据库（Milvus 或 pgvector），前端通过 SSE 实现流式输出。

## 常用命令

### 后端（在 `backend/` 目录下执行）

**注意：后端已重构为多模块 Maven 项目，包含 5 个子模块：geosmart-llm、geosmart-rag、geosmart-tools、geosmart-chat、geosmart-api**

```bash
# 在父项目根目录（backend/）执行所有命令
mvn compile                          # 编译所有模块
mvn test                             # 运行所有模块测试
mvn test -Dtest=TestClassName        # 运行单个测试类
mvn test -Dtest=TestClassName#method # 运行单个测试方法
mvn spring-boot:run                  # 启动开发服务器（端口 8080，从 geosmart-api 模块）
mvn clean install -DskipTests        # 完整构建并安装到本地仓库
mvn clean package -DskipTests        # 打包 JAR（跳过测试）
mvn checkstyle:check                 # 运行代码规范检查

# 多模块特定操作
mvn clean install -pl geosmart-api -am  # 仅构建 API 模块及其依赖
mvn test -pl geosmart-llm                      # 仅运行 LLM 模块测试
```

### 前端（在 `frontend/` 目录下执行）

```bash
npm run dev          # 开发服务器 + HMR（端口 5173，/api 代理至 localhost:8080）
npm run build        # 类型检查 + 生产构建
npm run type-check   # 仅 TypeScript 类型检查（vue-tsc）
npm run lint         # 先运行 oxlint，再运行 eslint（自动修复）
npm run lint:eslint  # 仅 ESLint
npm run lint:oxlint  # 仅 Oxlint
```

Node 版本要求：`^20.19.0 || >=22.12.0`

### 全栈启动

1. 启动后端：`cd backend/geosmart-api && mvn spring-boot:run`
2. 启动前端：`cd frontend && npm run dev`
3. 打开 http://localhost:5173（Vite 自动将 API 请求代理至后端）

## 架构

```
frontend (Vue 3, 端口 5173)
  │  SSE 流式传输 + REST API
  │  Vite 代理: /api → localhost:8080
  ▼
backend (Spring Boot 3.5, 多模块架构, 端口 8080)
  ├── geosmart-llm/      — LLM 提供商配置（智谱、DeepSeek、OpenAI）
  ├── geosmart-rag/      — RAG 管道（文档摄入、向量化、检索）
  ├── geosmart-tools/    — Agent Tools（法规检索、空间查询、业务状态）
  ├── geosmart-chat/     — Chat 装配（AiService、ChatConfig、ChatMemory）
  └── geosmart-api/      — REST API（SSE 流式、文档上传、会话管理）
```

### 模块依赖关系

```
geosmart-api (Spring Boot 应用)
  ├── 依赖: geosmart-chat
  ├── 依赖: geosmart-tools
  └── 依赖: geosmart-rag

geosmart-chat
  ├── 依赖: geosmart-tools
  └── 依赖: geosmart-rag

geosmart-tools
  ├── 依赖: geosmart-rag
  └── 依赖: geosmart-llm

geosmart-rag
  └── 依赖: geosmart-llm

geosmart-llm (基础模块，无其他依赖)
```

### 核心集成点

- **ChatConfig.java**（在 geosmart-chat 模块）是核心装配入口：构建 `AiServices`，注册工具、`ContentRetriever`（RAG）和 `ChatMemoryProvider`（会话管理）。新增工具或修改 RAG 配置时需修改此文件。
- **GeoSmartAssistant.java**（在 geosmart-chat 模块）定义了 `@SystemMessage` 系统提示词和 `chat()` 接口 — 这是 LLM 的调用契约。
- **ChatController.java**（在 geosmart-api 模块）处理 SSE 流式传输和 HTTP 请求。
- **前端 Pinia store**（`stores/chat.ts`）管理 SSE 流式状态；原始 SSE 解析逻辑在 `api/chat.ts` 中。

### LLM 提供商切换

通过 `application.yml` 中的 `llm.provider` 控制（默认：`zhipu`）。可通过环境变量 `LLM_PROVIDER=zhipu|deepseek|openai` 覆盖。每个提供商需要对应的 `*_API_KEY` 环境变量。`LlmConfig.java` 根据提供商名称选择 `ChatModel` Bean。

### RAG 管道

文档（PDF/DOCX/TXT）→ 解析 → 递归分块（500 字符，100 重叠）→ 向量化（AllMiniLmL6-v2 ONNX，本地模型）→ InMemoryEmbeddingStore。检索使用 `EmbeddingStoreContentRetriever`，`maxResults: 5`。配置项在 `application.yml` 的 `rag.*` 下。

### Agent 工具

工具是 Spring `@Component` 类，方法上标注 LangChain4j 的 `@Tool` 注解。当前返回硬编码的模拟数据。新增工具时，在 `ChatConfig.tools(...)` 中注册 Bean 即可。

### 聊天流式传输

后端使用 `SseEmitter` 配合 LangChain4j 的 `TokenStream` API。前端通过 `fetch()` + `ReadableStream` 读取，解析 `data:{token}\n\n` 格式的 SSE 数据。

## 配置

所有配置集中在 `backend/geosmart-api/src/main/resources/application.yml`。关键配置项：

- `llm.provider` / `llm.{provider}.api-key` — LLM 提供商选择及 API 密钥
- `rag.chunk-size`、`rag.chunk-overlap`、`rag.max-results` — RAG 参数调优
- `chat.max-memory-messages` — 单会话记忆窗口（默认 20 条）
- 示例文档自动加载目录：`backend/geosmart-api/src/main/resources/sample-docs/`

## 编码规范

- **后端开发**：Java 17+。严禁在 Controller 层写业务逻辑，必须委托给 Service。
- **LangChain4j 规范**：使用 `@SystemMessage` 严格定义客服人设，强制要求"引用政策需注明文件名称"。使用 `@AiService` 声明大脑，使用 `@Tool` 暴露现有的 Service API。
- **权限与安全（关键）**：在 `@Tool` 暴露的方法内，**必须**硬编码权限透传逻辑，校验当前操作用户的 Session 防止越权，并在返回给 AI 之前剔除敏感字段（如业主电话）。
- **RAG 处理**：注意智能切片（Chunking）的独立性（500-1000 字/段）及混合检索（Hybrid Search）的实现。
- **前端交互**：遵循 Vue 函数式组件与 Hooks 规范。处理流式响应时，确保逐字渲染平滑，且不对未完成的 Markdown 代码块造成页面崩溃。
- **可观测性**：涉及调用大模型的代码链路，注意保留耗时、Token 消耗的日志输出点。

## 测试

后端测试使用 JUnit 5 + AssertJ + Mockito + Spring Boot Test（`@SpringBootTest`、`@WebMvcTest`）。测试代码位于 `backend/src/test/java/com/geosmart/`，与源码包结构一一对应。

前端尚未安装测试框架。

### 测试要求

1. **重大更改**（新增业务 Tool、RAG 检索逻辑重写、前端流式对话 UI）：
   - 必须进行功能测试。后端使用 Postman/curl 验证 API 和流式输出，前端使用 MCP Playwright 打开浏览器验证页面操作。
   - 验证大模型是否能正确触发 Tool Call（如准确提取"地块识别码"并调用 `getPlotDetail`）。
   - 验证敏感数据脱敏和越权查询的拦截机制。

2. **次要更改**（错误修复、样式调整、切片算法优化）：
   - 通过 JUnit 单元测试验证逻辑，通过 linter 检查前端规范。
   - 如果有疑问，请进行全面的端到端对话测试。

3. **所有更改必须通过：**
   - `mvn checkstyle:check`（后端）和 `npm run lint`（前端）无错误。
   - `mvn clean package` 编译成功。
   - 单元/功能测试通过。

## 强制工作流程

每个开发任务必须严格遵循以下工作流程，不得跳过任何步骤。

### 第 1 步：初始化环境

在开始任何开发工作之前，必须确保全栈环境正在运行且稳定：

```bash
# 后端依赖安装与编译
cd backend && mvn clean install

# 前端依赖安装
cd ../frontend && npm install
```

验证清单：
- 后端服务在 `http://localhost:8080` 正常响应
- 前端开发服务正常启动（端口 5173）
- 向量数据库（如已配置）连接正常
- 如使用 Docker 依赖（Milvus 等），确认 `docker-compose up -d` 已执行

**不要跳过此步骤。** 环境不稳定时继续开发只会浪费时间。

### 第 2 步：选择任务

从 `docs/` 目录下读取任务跟踪文件，选择下一个任务。

**任务文件结构**（支持多模块）：
```
docs/
├── tasks.json          # 核心模块任务
├── progress.md         # 核心模块进度日志
└── {module-name}/      # 其他模块（每个模块独立目录）
    ├── tasks.json
    └── progress.md
```

- 根目录下的 `docs/tasks.json` 和 `docs/progress.md` 是核心（core）模块的任务与进度。
- 新增模块时，在 `docs/` 下创建以模块名命名的子目录，各自维护独立的 `tasks.json` 和 `progress.md`。
- 在 `docs/tasks.json` 的 `modules` 数组中注册新模块的名称和文件路径。

选择标准（按优先级排序）：
1. 选择状态为 `status: "todo"` 的任务。
2. 遵循阶段依赖关系：**阶段 1（数据采集清洗入库）→ 阶段 2（逻辑开发）→ 阶段 3（交互优化）**，前一阶段未完成不得跳到下一阶段。
3. 在同阶段内，选择优先级最高的未完成任务。

### 第 3 步：实现任务

- 仔细阅读任务描述和步骤。
- 严格遵守 **LangChain4j** 的开发规范：使用 `@AiService` 声明智能服务，使用 `@Tool` 暴露现有 Service API。
- 处理 RAG 任务时，注意智能切片（Chunking）的独立性（500-1000 字/段）及混合检索（Hybrid Search）的实现。
- 前端实现流式输出时，需配合后端的 SSE 接口，确保逐字渲染平滑。
- 涉及 `@Tool` 方法时，**必须**实现权限透传和敏感字段脱敏。

### 第 4 步：彻底测试

实现完成后，根据更改级别进行对应测试：

**重大更改**（新增业务 Tool、RAG 检索逻辑重写、前端流式对话 UI）：
- **必须进行功能测试！** 后端使用 Postman/curl 验证 API 和流式输出，前端使用 MCP Playwright 打开浏览器验证页面操作。
- 验证大模型是否能正确触发 Tool Call（如准确提取"地块识别码"并调用对应方法）。
- 验证敏感数据脱敏和越权查询的拦截机制。

**次要更改**（错误修复、样式调整、切片算法优化）：
- 通过 JUnit 单元测试验证逻辑，通过 linter 检查前端规范。
- 如果有疑问，请进行全面的端到端对话测试。

**所有更改必须通过的门槛：**
- `mvn checkstyle:check`（后端）和 `npm run lint`（前端）无错误
- `mvn clean package` 编译成功
- 单元/功能测试全部通过

### 第 5 步：更新进度

将工作成果写入对应模块的 `progress.md`（位于 `docs/` 或 `docs/{module-name}/` 下），格式如下：

```text
## [日期] - 任务：[任务描述]

### 完成内容：
- [所做的具体更改，例如：封装了 LandPlotTools 的地块查询 Tool，并配置了提示词]

### 测试：
- [如何测试和验证，例如：Mock 了地块 A1 的数据，验证大模型成功生成最终合成回答]

### 备注：
- [架构决策，例如：由于国土名词精确度要求高，开启了混合检索与 Rerank 机制]
```

### 第 6 步：提交更改

**重要：所有更改必须在单个提交中提交，包括对任务跟踪文件的更新！**

提交前检查清单：
1. 对应模块的任务文件（`docs/tasks.json` 或 `docs/{module-name}/tasks.json`）中任务状态已更新为 `completed`。
2. 对应模块的进度文件（`docs/progress.md` 或 `docs/{module-name}/progress.md`）已更新。
3. 所有测试已通过。

一次性提交所有更改：

```bash
git add .
git commit -m "[任务描述] - 已完成"
```

**规则：**
- 仅当所有测试步骤都通过时才标记任务完成。
- 代码、进度日志和任务跟踪器的更新必须在同一个提交中捆绑。

## 阻塞问题处理

如果任务无法继续，需要人工干预时，遵循以下规则：

### 常见阻塞场景

1. **缺少环境配置**：`application.yml` 或 `.env` 中缺少 LLM API Key；向量数据库无法连接。
2. **外部依赖不可用**：国土规划 PDF/Docx 原始语料文件缺失；后端被调用的真实业务 Service（如 GIS 地图 API、审批流接口）无法访问。
3. **合规与安全阻碍**：遇到无法自动绕过的 Session 权限校验。

### 阻塞时的规则

**禁止：**
- 不要提交 git 提交。
- 不要将任务标记为完成。
- 不要用伪造的 API Key 或模拟数据强行替代核心流程（除非任务明确要求编写 Mock）。

**应当：**
- 记录当前进度和确切的阻塞原因。
- 输出明确的阻塞消息，说明阻塞原因和人工需要采取的行动，等待人工干预。