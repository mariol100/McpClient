package ca.mlapp.dev.McpClient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmConfig {
    private boolean enabled;
    private String defaultProvider;
    private AnthropicConfig anthropic;
    private OpenAiConfig openai;
    private OllamaConfig ollama;

    @Data
    public static class AnthropicConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        private int maxTokens;
        private double temperature;
    }

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
        private int maxTokens;
        private double temperature;
    }

    @Data
    public static class OllamaConfig {
        private boolean enabled;
        private String baseUrl;
        private String model;
    }
}
