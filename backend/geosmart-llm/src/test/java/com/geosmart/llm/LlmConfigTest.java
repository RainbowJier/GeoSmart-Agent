package com.geosmart.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LlmConfig bean creation.
 * Uses @SpringBootTest with a minimal test configuration class
 * that provides @SpringBootApplication semantics.
 */
@SpringBootTest(classes = LlmConfigTest.TestApplication.class)
@TestPropertySource(properties = {
    "llm.provider=zhipu",
    "llm.zhipu.api-key=test-key",
    "llm.zhipu.model-name=glm-4-flash",
    "llm.deepseek.api-key=test-key",
    "llm.deepseek.model-name=deepseek-chat",
    "llm.openai.api-key=sk-test",
    "llm.openai.model-name=gpt-4o"
})
class LlmConfigTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Test
    void chatModelBeanShouldBeCreated() {
        assertThat(chatModel).isNotNull();
    }

    @Test
    void streamingChatModelBeanShouldBeCreated() {
        assertThat(streamingChatModel).isNotNull();
    }

    /**
     * Minimal test configuration that provides @SpringBootApplication semantics.
     * This enables the Spring Boot test context to bootstrap properly.
     */
    @Configuration
    @EnableAutoConfiguration
    @EnableConfigurationProperties(LlmProperties.class)
    @Import(LlmConfig.class)
    static class TestApplication {
    }
}
