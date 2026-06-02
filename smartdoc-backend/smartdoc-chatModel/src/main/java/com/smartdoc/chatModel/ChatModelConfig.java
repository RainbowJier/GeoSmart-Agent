package com.smartdoc.chatModel;

import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ChatModelProperties.class)
public class ChatModelConfig {

    private final ChatModelProperties chatModelProperties;

    private static final String ZHIPU = "zhipu";

    @Bean
    public ChatModel chatLanguageModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing ChatModel: provider={}, model={}", provider, config.getModelName());
        return buildChatModel(provider, config);
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing StreamingChatModel: provider={}, model={}", provider, config.getModelName());
        return buildStreamingChatModel(provider, config);
    }

    private ChatModel buildChatModel(String provider, ChatModelProperties.ProviderConfig config) {
        if (ZHIPU.equals(provider)) {
            return ZhipuAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .maxToken(config.getMaxTokens())
                    .build();
        }
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .reasoningEffort("high")
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .build();
    }

    private StreamingChatModel buildStreamingChatModel(String provider, ChatModelProperties.ProviderConfig config) {
        if (ZHIPU.equals(provider)) {
            return ZhipuAiStreamingChatModel.builder()
                    .apiKey(config.getApiKey())
                    .model(config.getModelName())
                    .maxToken(config.getMaxTokens())
                    .build();
        }
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .reasoningEffort("high")
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .build();
    }
}
