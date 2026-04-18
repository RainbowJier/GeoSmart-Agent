# GeoSmart-Agent 后端模块化重构设计

**日期**: 2026-04-18
**作者**: Claude
**状态**: 已批准

---

## 1. 概述

将当前单体 Maven 项目重构为多模块架构，实现清晰的职责分离和依赖管理。

### 1.1 目标

- 将 RAG、Tools、Chat、LLM 功能拆分为独立模块
- 保持单向依赖关系，避免循环依赖
- 提高代码可维护性和可测试性
- 为未来微服务演进预留可能性

### 1.2 当前结构问题

- 所有代码在一个模块中，包结构逻辑上分离但无强制边界
- 依赖关系隐式存在，难以强制约束
- 难以独立测试某个子功能

---

## 2. 模块架构

### 2.1 模块列表

| 模块 | Maven Artifact | 职责 |
|------|----------------|------|
| geosmart-llm | `geosmart-llm` | LLM 提供商配置和切换 |
| geosmart-rag | `geosmart-rag` | 文档摄入和向量检索 |
| geosmart-tools | `geosmart-tools` | Agent 工具集合 |
| geosmart-chat | `geosmart-chat` | 会话记忆管理 |
| geosmart-api | `geosmart-api` | REST API 和装配中心（可执行） |

### 2.2 依赖关系

```
geosmart-api
  ├── 依赖 geosmart-llm
  ├── 依赖 geosmart-rag
  ├── 依赖 geosmart-tools
  └── 依赖 geosmart-chat

其他模块之间：无相互依赖（LLM 模块被 RAG 依赖是为了 EmbeddingModel）
```

**依赖规则**：
- API 模块可以依赖所有其他模块
- RAG 模块可以依赖 LLM 模块（需要 EmbeddingModel）
- 其他模块之间不允许相互依赖
- 工具模块和聊天模块之间不允许相互依赖

---

## 3. 各模块详细设计

### 3.1 geosmart-llm

**职责**：LLM 提供商配置和切换

**包结构**：
```
com.geosmart.llm
├── LlmConfig.java           — LLM Bean 配置
├── LlmProperties.java       — 配置属性绑定
└── provider/                — 各提供商实现（可选）
```

**主要类**：
- `LlmProperties` - 绑定 `application.yml` 中的 `llm.*` 配置
- `LlmConfig` - 创建 `ChatModel` 和 `StreamingChatModel` Bean

**对外暴露**：
- `ChatModel` Bean
- `StreamingChatModel` Bean
- `EmbeddingModel` Bean（供 RAG 模块使用）

**依赖**：
- LangChain4j 核心库
- LangChain4j OpenAI 模块
- LangChain4j 智谱 AI 模块
- Spring Boot Configuration

---

### 3.2 geosmart-rag

**职责**：文档摄入、分块、向量化、检索

**包结构**：
```
com.geosmart.rag
├── DocumentIngestionService.java  — 文档摄入服务
├── EmbeddingConfig.java          — 向量化配置
├── RetrievalService.java         — 检索服务
└── chunker/                      — 分块器（可选扩展）
```

**主要类**：
- `DocumentIngestionService` - 解析 PDF/DOCX/TXT，分块，向量化
- `EmbeddingConfig` - 创建 `EmbeddingModel` 和 `EmbeddingStore` Bean
- `RetrievalService` - 封装 `ContentRetriever`，提供检索接口

**对外暴露**：
- `ContentRetriever`（通过 RetrievalService）
- `DocumentIngestionService`

**依赖**：
- geosmart-llm（使用 EmbeddingModel）
- LangChain4j 文档解析器
- LangChain4j Embeddings

---

### 3.3 geosmart-tools

**职责**：Agent 工具集合

**包结构**：
```
com.geosmart.agent
├── tools/
│   ├── RegulationSearchTool.java   — 法规检索工具
│   ├── SpatialQueryTool.java       — 空间查询工具
│   └── BusinessStatusTool.java     — 业务状态工具
└── Tool (interface, optional)
```

**主要类**：
- `RegulationSearchTool` - 法规政策检索
- `SpatialQueryTool` - 空间规划信息查询
- `BusinessStatusTool` - 业务办理状态查询

**对外暴露**：
- 所有 `@Component` 标注的 Tool 类（Spring 自动扫描）

**依赖**：
- LangChain4j 核心库
- Spring Boot（@Component 支持）

**注意**：
- 当前返回硬编码数据，未来接入真实数据源时需要添加 Repository 依赖
- 必须实现权限校验和数据脱敏

---

### 3.4 geosmart-chat

**职责**：会话记忆管理

**包结构**：
```
com.geosmart.chat
└── ChatSessionManager.java  — 会话管理器
```

**主要类**：
- `ChatSessionManager` - 管理多会话的 ChatMemory，支持滑动窗口

**对外暴露**：
- `ChatSessionManager` Bean
- `ChatMemoryProvider` 接口实现（可选）

**依赖**：
- LangChain4j 核心库
- Spring Boot

**未来扩展**：
- 支持持久化存储（Redis、数据库）
- 支持多种记忆策略（全量、滑动窗口、摘要）

---

### 3.5 geosmart-api

**职责**：REST API 和装配中心

**包结构**：
```
com.geosmart
├── GeoSmartApplication.java    — 启动类
├── config/
│   └── AppConfig.java          — 应用配置
├── api/
│   ├── ChatController.java     — SSE 聊天端点
│   ├── DocumentController.java — 文档上传端点
│   └── dto/
│       └── ChatRequest.java    — 请求 DTO
└── chat/
    ├── ChatConfig.java         — AI 服务装配
    └── GeoSmartAssistant.java  — AI 服务接口
```

**主要类**：
- `GeoSmartApplication` - Spring Boot 启动类
- `ChatConfig` - 装配 GeoSmartAssistant，注入所有依赖
- `GeoSmartAssistant` - AI 服务接口定义（@SystemMessage）
- `ChatController` - SSE 流式聊天
- `DocumentController` - 文档上传

**依赖**：
- geosmart-llm
- geosmart-rag
- geosmart-tools
- geosmart-chat

**打包**：
- `spring-boot-maven-plugin` 配置为可执行 JAR
- 主类：`com.geosmart.GeoSmartApplication`

---

## 4. Maven 配置

### 4.1 父 POM (geosmart-agent/pom.xml)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
    </parent>

    <groupId>com.geosmart</groupId>
    <artifactId>geosmart-agent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <java.version>17</java.version>
        <langchain4j.version>1.13.0</langchain4j.version>
        <langchain4j-beta.version>1.13.0-beta23</langchain4j-beta.version>
    </properties>

    <modules>
        <module>geosmart-llm</module>
        <module>geosmart-rag</module>
        <module>geosmart-tools</module>
        <module>geosmart-chat</module>
        <module>geosmart-api</module>
    </modules>

    <dependencyManagement>
        <!-- 统一管理依赖版本 -->
    </dependencyManagement>
</project>
```

### 4.2 子模块 POM 模板

每个子模块：
```xml
<parent>
    <groupId>com.geosmart</groupId>
    <artifactId>geosmart-agent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</parent>
<artifactId>geosmart-{module}</artifactId>
<packaging>jar</packaging>
```

---

## 5. 迁移策略

### 5.1 迁移顺序

1. **创建父 POM 和模块结构** - 建立骨架
2. **迁移 geosmart-llm** - 无依赖，优先迁移
3. **迁移 geosmart-chat** - 无依赖，独立性强
4. **迁移 geosmart-tools** - 无依赖
5. **迁移 geosmart-rag** - 依赖 llm 模块
6. **迁移 geosmart-api** - 最后迁移，依赖所有模块

### 5.2 代码移动规则

- 保持包名不变（`com.geosmart.*`）- 避免大范围代码修改
- 只调整模块间的 pom.xml 依赖
- `@Configuration` 类随功能模块移动

### 5.3 配置文件

- `application.yml` 保留在 `geosmart-api/src/main/resources/`
- 各模块的配置前缀保持不变（`llm.*`, `rag.*`, `chat.*`）

---

## 6. 测试策略

### 6.1 单元测试

- 每个模块保持独立的单元测试
- Mock 跨模块的依赖

### 6.2 集成测试

- `geosmart-api` 模块包含全栈集成测试
- 验证模块间的装配正确

### 6.3 测试命令

```bash
# 父目录运行所有测试
mvn test

# 单独测试某个模块
cd geosmart-llm && mvn test
```

---

## 7. 未来扩展

### 7.1 短期（本次重构后）

1. **配置外部化** - 将 LLM API Key 等敏感配置外部化
2. **测试完善** - 补充各模块的单元测试覆盖率

### 7.2 中期

1. **geosmart-common 模块** - 如果需要共享 DTO/工具类
2. **持久化支持** - ChatSessionManager 支持 Redis
3. **真实数据源** - Tools 接入真实数据库

### 7.3 长期（微服务演进）

1. **独立部署** - 各模块可独立部署为服务
2. **服务拆分** - API 网关 + 多个微服务
3. **事件驱动** - 使用消息队列解耦模块

---

## 8. 验收标准

### 8.1 功能验收

- [ ] 所有现有功能正常运行
- [ ] SSE 聊天接口正常工作
- [ ] 文档上传和检索正常工作
- [ ] Agent 工具能被 LLM 正确调用
- [ ] 会话管理功能正常

### 8.2 构建验收

- [ ] `mvn clean install` 在父目录成功构建
- [ ] `mvn test` 所有测试通过
- [ ] `geosmart-api/target/geosmart-api-0.1.0-SNAPSHOT.jar` 可独立运行

### 8.3 代码质量验收

- [ ] `mvn checkstyle:check` 无错误
- [ ] 无循环依赖
- [ ] 每个模块职责单一

---

## 9. 风险和缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 配置文件路径变化 | 启动失败 | 明确配置文件位置，更新文档 |
| Bean 扫描问题 | 注入失败 | 调整 `@ComponentScan` 配置 |
| 循环依赖 | 构建失败 | 严格遵循单向依赖设计 |

---

## 附录 A：目录结构对比

### 当前结构
```
backend/
├── pom.xml
└── src/main/java/com/geosmart/
    ├── api/
    ├── agent/tools/
    ├── chat/
    ├── config/
    ├── llm/
    └── rag/
```

### 重构后结构
```
backend/
├── pom.xml (父 POM)
├── geosmart-llm/
│   ├── pom.xml
│   └── src/main/java/com/geosmart/llm/
├── geosmart-rag/
│   ├── pom.xml
│   └── src/main/java/com/geosmart/rag/
├── geosmart-tools/
│   ├── pom.xml
│   └── src/main/java/com/geosmart/agent/tools/
├── geosmart-chat/
│   ├── pom.xml
│   └── src/main/java/com/geosmart/chat/
└── geosmart-api/
    ├── pom.xml
    └── src/main/java/com/geosmart/
        ├── api/
        ├── config/
        └── chat/
```
