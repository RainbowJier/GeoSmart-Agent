package com.smartdoc.chatModel;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置
 */
@Slf4j
@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Create local Embedding Bean.
     * <p>
     * AllMiniLmL6V2 is a local model in ONNX format, and does not require network access.
     * Suitable for local development and teaching environments.
     * Production environment can be replaced with a remote Embedding API.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing EmbeddingModel: AllMiniLmL6V2 (local ONNX model)");
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
