package com.geosmart.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for LlmProperties.
 * These tests don't require Spring context - they only test POJO logic.
 */
class LlmPropertiesTest {

    @Test
    void llmProperties_bindsDefaultsCorrectly() {
        LlmProperties props = new LlmProperties();

        assertThat(props.getProvider()).isEqualTo("zhipu");
        assertThat(props.getDeepseek()).isNotNull();
        assertThat(props.getOpenai()).isNotNull();
        assertThat(props.getZhipu()).isNotNull();
    }

    @Test
    void getActiveConfig_returnsZhipuByDefault() {
        LlmProperties props = new LlmProperties();

        LlmProperties.ProviderConfig zhipu = props.getZhipu();
        zhipu.setBaseUrl("https://open.bigmodel.cn/");
        zhipu.setApiKey("test-key");
        zhipu.setModelName("glm-4-flash");

        LlmProperties.ProviderConfig active = props.getActiveConfig();
        assertThat(active.getBaseUrl()).isEqualTo("https://open.bigmodel.cn/");
        assertThat(active.getApiKey()).isEqualTo("test-key");
        assertThat(active.getModelName()).isEqualTo("glm-4-flash");
    }

    @Test
    void getActiveConfig_returnsDeepseekWhenConfigured() {
        LlmProperties props = new LlmProperties();
        props.setProvider("deepseek");

        LlmProperties.ProviderConfig deepseek = props.getDeepseek();
        deepseek.setBaseUrl("https://api.deepseek.com");
        deepseek.setApiKey("test-key");
        deepseek.setModelName("deepseek-chat");

        LlmProperties.ProviderConfig active = props.getActiveConfig();
        assertThat(active.getBaseUrl()).isEqualTo("https://api.deepseek.com");
        assertThat(active.getApiKey()).isEqualTo("test-key");
        assertThat(active.getModelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void getActiveConfig_returnsOpenaiWhenConfigured() {
        LlmProperties props = new LlmProperties();
        props.setProvider("openai");

        LlmProperties.ProviderConfig openai = props.getOpenai();
        openai.setBaseUrl("https://api.openai.com");
        openai.setApiKey("sk-test");
        openai.setModelName("gpt-4o");

        LlmProperties.ProviderConfig active = props.getActiveConfig();
        assertThat(active.getBaseUrl()).isEqualTo("https://api.openai.com");
        assertThat(active.getApiKey()).isEqualTo("sk-test");
        assertThat(active.getModelName()).isEqualTo("gpt-4o");
    }

    @Test
    void getActiveConfig_isCaseInsensitive() {
        LlmProperties props = new LlmProperties();
        props.setProvider("ZHIPU");

        LlmProperties.ProviderConfig zhipu = props.getZhipu();
        zhipu.setBaseUrl("https://open.bigmodel.cn/");
        zhipu.setApiKey("test-key");
        zhipu.setModelName("glm-4-flash");

        assertThat(props.getActiveConfig().getBaseUrl()).isEqualTo("https://open.bigmodel.cn/");
    }
}
