package com.smartdoc.chatModel;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${rag.embedding.type:local}") String type,
            @Value("${llm.providers.zhipu.api-key:}") String zhipuApiKey) {
        if ("zhipu".equalsIgnoreCase(type)) {
            if (zhipuApiKey.isBlank() || zhipuApiKey.contains("your-api-key-here")) {
                log.warn("Zhipu embedding requested but API key not configured, falling back to local model");
            } else {
                log.info("Initializing EmbeddingModel: ZhipuAi remote (Chinese optimized)");
                return ZhipuAiEmbeddingModel.builder()
                        .apiKey(zhipuApiKey)
                        .build();
            }
        }
        log.info("Initializing EmbeddingModel: AllMiniLmL6V2 (local ONNX model, English-focused)");
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
