# GeoSmart-Agent

面向国土空间规划的全栈智能助手。通过 RAG（检索增强生成）实现政策咨询，通过 Agent（智能体工具调用）实现业务办理。

**核心痛点：** 国土法规更新快、查询难，业务系统操作门槛高。
**解决方案：** RAG 解决知识储备 + Agent 解决业务执行。

## 功能特性

- **智能对话** — 基于 LLM 的自然语言交互，SSE 流式响应实时显示
- **政策咨询（RAG）** — 上传 PDF/DOCX/TXT 文档，自动解析分块、向量化存储，检索增强生成回答
- **业务办理（Agent Tools）** — 法规检索、空间查询、业务进度查询等工具调用
- **多会话管理** — 支持多轮对话、会话新建/切换、历史记录持久化
- **文档上传** — 支持 PDF、DOCX、TXT 格式，自动入库供 RAG 检索
- **多 LLM 提供商** — 支持智谱 GLM、DeepSeek、OpenAI，通过配置切换

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.5 + Java 17 + LangChain4j 1.13 |
| 前端 | Vue 3 + TypeScript + Vite 8 + Element Plus + Pinia |
| AI | LangChain4j（ChatModel、RAG、Tool、ChatMemory） |
| 向量模型 | All-MiniLM-L6-v2（ONNX 本地推理） |
| 文档解析 | Apache PDFBox + Apache POI |

## 项目结构

```
GeoSmart-Agent/
├── backend/                          # Spring Boot 多模块后端
│   ├── geosmart-llm/                 # LLM 提供商配置模块
│   ├── geosmart-rag/                 # RAG 管道模块
│   ├── geosmart-tools/               # Agent 工具模块
│   ├── geosmart-chat/                # Chat 装配模块
│   ├── geosmart-api/                 # REST API 模块（可执行 JAR）
│   └── pom.xml                       # 父 POM 配置
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── views/ChatView.vue        # 聊天主界面
│       ├── stores/chat.ts            # Pinia 状态管理
│       └── api/chat.ts               # SSE 流式 API 客户端
├── CLAUDE.md                         # Claude Code 工作指导
└── README.md
```

### 后端模块说明

- **geosmart-llm**: LLM 提供商配置（支持智谱 GLM、DeepSeek、OpenAI）
- **geosmart-rag**: RAG 检索增强生成（文档解析、向量化、检索）
- **geosmart-tools**: Agent 工具集（法规检索、空间查询、业务状态查询）
- **geosmart-chat**: Chat 服务装配（AiService、会话管理、工具集成）
- **geosmart-api**: REST API 层（SSE 流式传输、文档上传、会话管理）

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 20.19+ 或 22.12+
- LLM API Key（智谱 / DeepSeek / OpenAI 任选其一）

### 配置

编辑 `backend/src/main/resources/application.yml`，设置 LLM API Key：

```yaml
llm:
  provider: zhipu                    # 可选：zhipu | deepseek | openai
  zhipu:
    api-key: YOUR_ZHIPU_API_KEY
```

也可通过环境变量覆盖：`LLM_PROVIDER=zhipu`、`ZHIPU_API_KEY=xxx`。

### 启动

**1. 启动后端**

```bash
cd backend/geosmart-api
mvn spring-boot:run                  # 端口 8080

# 或者使用可执行 JAR
cd backend
java -jar geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar
```

**2. 启动前端**

```bash
cd frontend
npm install
npm run dev                          # 端口 5173，/api 代理至 localhost:8080
```

**3. 访问**

打开 http://localhost:5173

## 架构

```
Vue 3 (5173)  ──SSE/REST──▶  Spring Boot 多模块架构 (8080)
                                  ├── geosmart-api (REST 控制器)
                                  ├── geosmart-chat (AiService 装配)
                                  ├── geosmart-tools (Agent 工具)
                                  ├── geosmart-rag (RAG 管道)
                                  └── geosmart-llm (LLM 提供商)
```

**模块依赖关系：**
```
geosmart-api
  ├── geosmart-chat
  │   ├── geosmart-tools
  │   │   ├── geosmart-rag
  │   │   │   └── geosmart-llm
  │   │   └── geosmart-llm
  │   └── geosmart-rag
  └── geosmart-tools
```

**核心集成点：**
- `ChatConfig.java` (geosmart-chat) — 装配 AiServices，注册工具、ContentRetriever、ChatMemoryProvider
- `GeoSmartAssistant.java` (geosmart-chat) — 定义系统提示词和 `chat()` 接口契约
- `ChatController.java` (geosmart-api) — SSE 流式传输和 HTTP 请求处理
- `ChatView.vue` — 前端聊天界面，通过 `ReadableStream` 解析 SSE 流式数据

## 常用命令

```bash
# 后端（多模块构建）
cd backend
mvn compile                          # 编译所有模块
mvn test                             # 运行所有模块测试
mvn test -Dtest=TestClassName        # 运行单个测试类
mvn spring-boot:run                  # 启动开发服务器（从 geosmart-api 模块）
mvn clean install -DskipTests        # 完整构建并安装到本地仓库
mvn clean package -DskipTests        # 打包可执行 JAR

# 前端
cd frontend
npm run dev                          # 开发服务器 + HMR
npm run build                        # 类型检查 + 生产构建
npm run type-check                   # 仅 TypeScript 类型检查
npm run lint                         # oxlint + eslint
```

## License

Private — Internal Use Only
