package com.geosmart.chat;

import com.geosmart.agent.tools.BusinessStatusTool;
import com.geosmart.agent.tools.RegulationSearchTool;
import com.geosmart.agent.tools.SpatialQueryTool;
import com.geosmart.rag.RetrievalService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public GeoSmartAssistant geoSmartAssistant(
            ChatModel chatLanguageModel,
            StreamingChatModel streamingChatModel,
            RetrievalService retrievalService,
            ChatSessionManager sessionManager,
            RegulationSearchTool regulationSearchTool,
            SpatialQueryTool spatialQueryTool,
            BusinessStatusTool businessStatusTool) {

        return AiServices.builder(GeoSmartAssistant.class)
                .chatModel(chatLanguageModel)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(retrievalService.getContentRetriever())
                .chatMemoryProvider((ChatMemoryProvider) memoryId ->
                        sessionManager.getOrCreate(memoryId.toString()))
                .tools(regulationSearchTool, spatialQueryTool, businessStatusTool)
                .build();
    }
}
