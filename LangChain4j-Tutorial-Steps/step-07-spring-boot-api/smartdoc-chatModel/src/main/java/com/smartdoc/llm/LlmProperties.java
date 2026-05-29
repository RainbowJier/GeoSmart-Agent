package com.smartdoc.llm;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM configuration properties bound to the "llm" prefix in application.yml.
 *
 * Supports multiple providers (zhipu, deepseek) with runtime switching.
 * Each provider configures its own baseUrl, apiKey, and modelName independently.
 *
 * Comparison with Step 06 (Plain Java):
 * Step 06 hardcoded provider settings inside Java code:
 *   {@code new ZhipuAiChatModel.builder().apiKey("...").model("glm-4-flash").build()}
 *
 * Spring Boot externalizes all provider configs to application.yml, making them
 * changeable without recompilation. Environment variable overrides are built in
 * via Spring's property resolution (e.g. ${ZHIPU_API_KEY}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * Active provider key: "zhipu" or "deepseek".
     * Overridable via environment variable {@code LLM_PROVIDER}.
     */
    private String provider = "zhipu";

    /**
     * Provider-specific configurations keyed by provider name.
     * This map-based approach allows adding new providers without changing Java code.
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();

    private ProviderConfig zhipu = new ProviderConfig();
    private ProviderConfig deepseek = new ProviderConfig();

    @PostConstruct
    void initProviders() {
        providers.put("zhipu", zhipu);
        providers.put("deepseek", deepseek);
    }

    /**
     * Get the configuration for the currently active provider.
     *
     * @return ProviderConfig for the active provider
     * @throws IllegalArgumentException if the provider name is unknown
     */
    public ProviderConfig getActiveConfig() {
        ProviderConfig config = providers.get(provider.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException(
                    "Unknown LLM provider: '" + provider + "'. Supported: " + providers.keySet());
        }
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException(
                    "API Key not configured for provider '" + provider + "'. " +
                    "Set environment variable " + provider.toUpperCase() + "_API_KEY or " +
                    "configure llm." + provider + ".api-key in application.yml");
        }
        return config;
    }

    /**
     * Connection settings for a single LLM provider.
     */
    @Getter
    @Setter
    public static class ProviderConfig {
        /** API base URL (required for OpenAI-compatible providers like DeepSeek). */
        private String baseUrl;
        /** API key. Use environment variables instead of hardcoding. */
        private String apiKey;
        /** Model name, e.g. glm-4-flash, deepseek-chat. */
        private String modelName;
    }
}
