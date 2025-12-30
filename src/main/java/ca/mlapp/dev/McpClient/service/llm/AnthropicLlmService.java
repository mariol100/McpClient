package ca.mlapp.dev.McpClient.service.llm;

import ca.mlapp.dev.McpClient.config.LlmConfig;
import ca.mlapp.dev.McpClient.dto.llm.LlmResponse;
import ca.mlapp.dev.McpClient.exception.LlmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicLlmService {
    private final LlmConfig llmConfig;
    private final WebClient.Builder webClientBuilder;

    public LlmResponse sendPrompt(String prompt, String model, Integer maxTokens, Double temperature) {
        long startTime = System.currentTimeMillis();

        try {
            // Build request body for Anthropic API
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model != null ? model : llmConfig.getAnthropic().getModel());
            requestBody.put("max_tokens", maxTokens != null ? maxTokens : llmConfig.getAnthropic().getMaxTokens());

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", List.of(message));

            if (temperature != null) {
                requestBody.put("temperature", temperature);
            } else if (llmConfig.getAnthropic().getTemperature() > 0) {
                requestBody.put("temperature", llmConfig.getAnthropic().getTemperature());
            }

            // Call Anthropic API
            WebClient webClient = webClientBuilder
                .baseUrl(llmConfig.getAnthropic().getBaseUrl())
                .defaultHeader("x-api-key", llmConfig.getAnthropic().getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();

            log.info("Calling Anthropic API with model: {}", requestBody.get("model"));

            Map<String, Object> response = webClient.post()
                .uri("/v1/messages")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // Parse response
            String responseText = extractAnthropicResponse(response);
            int tokensUsed = extractTokenCount(response);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Anthropic API call completed in {}ms, tokens used: {}", responseTime, tokensUsed);

            return new LlmResponse(
                "claude",
                model != null ? model : llmConfig.getAnthropic().getModel(),
                responseText,
                tokensUsed,
                responseTime
            );
        } catch (Exception e) {
            log.error("Error calling Anthropic API: {}", e.getMessage(), e);
            throw new LlmException("Failed to call Anthropic API: " + e.getMessage(), e);
        }
    }

    private String extractAnthropicResponse(Map<String, Object> response) {
        // Extract text from response.content[0].text
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content != null && !content.isEmpty()) {
            return (String) content.get(0).get("text");
        }
        return "";
    }

    private int extractTokenCount(Map<String, Object> response) {
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            Integer output = (Integer) usage.get("output_tokens");
            return output != null ? output : 0;
        }
        return 0;
    }
}
