package com.smartdoc.chat;

import com.smartdoc.agent.tools.KnowledgeSearchTool;
import com.smartdoc.agent.tools.TaskStatusTool;
import com.smartdoc.rag.RetrievalService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat configuration — AiService assembly core.
 * <p>
 * This is the most important comparison point with Step 06 (Plain Java).
 * In Step 06, AiServices was assembled manually in main():
 *
 * <pre>{@code
 * Assistant assistant = AiServices.builder(Assistant.class)
 *     .chatModel(chatModel)
 *     .streamingChatModel(streamingChatModel)
 *     .contentRetriever(contentRetriever)
 *     .chatMemoryProvider(memoryId -> sessionMap.getOrCreate(memoryId.toString()))
 *     .tools(toolObject1, toolObject2)
 *     .build();
 * }</pre>
 * <p>
 * Spring Boot approach: declarative assembly with @Configuration + @Bean.
 * All dependencies are injected automatically by the IoC container.
 * <p>
 * Key changes from Step 06:
 * - Manual object creation  → Spring auto-injects all Beans
 * - Hardcoded configuration → Externalized in application.yml
 * - Hard to test            → Each Bean can be mocked for unit tests
 * <p>
 * Every parameter in AiServices.builder() is a Spring Bean:
 * - chatModel:           provided by LlmConfig
 * - streamingChatModel:  provided by LlmConfig
 * - contentRetriever:    provided by RetrievalService
 * - chatMemoryProvider:  created dynamically via ChatSessionManager
 * - tools:               @Component-annotated tool classes
 *
 * @see AiServices
 * @see SpringCodeAssistant
 */
@Configuration
public class ChatConfig {

    /**
     * Create the DocAssistant AiService proxy bean.
     * <p>
     * This is the Spring Boot equivalent of Step 06's AiServices.builder() call.
     * All dependencies are injected as method parameters by the IoC container.
     *
     * @param chatModel           synchronous ChatModel (for Tool Call orchestration)
     * @param streamingChatModel  streaming ChatModel (for SSE token output)
     * @param retrievalService    provides the RAG ContentRetriever
     * @param sessionManager      manages per-user ChatMemory sessions
     * @param knowledgeSearchTool agent tool for knowledge base search
     * @param taskStatusTool      agent tool for business status queries
     * @return DocAssistant proxy, registered as a Spring Bean
     */
    @Bean
    public SpringCodeAssistant docAssistant(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            RetrievalService retrievalService,
            ChatSessionManager sessionManager,
            KnowledgeSearchTool knowledgeSearchTool,
            TaskStatusTool taskStatusTool) {

        return AiServices.builder(SpringCodeAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(retrievalService.getContentRetriever())
                .chatMemoryProvider(memoryId -> sessionManager.getOrCreate(memoryId.toString()))
                .tools(knowledgeSearchTool, taskStatusTool)
                .build();
    }
}
