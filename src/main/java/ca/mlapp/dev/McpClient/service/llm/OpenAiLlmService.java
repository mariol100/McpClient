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
public class OpenAiLlmService {
    private final LlmConfig llmConfig;
    private final WebClient.Builder webClientBuilder;

    public LlmResponse sendPrompt(String prompt, String model, Integer maxTokens, Double temperature) {
        long startTime = System.currentTimeMillis();

        try {
            // Build request body for OpenAI API
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model != null ? model : llmConfig.getOpenai().getModel());
            requestBody.put("max_tokens", maxTokens != null ? maxTokens : llmConfig.getOpenai().getMaxTokens());

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            requestBody.put("messages", List.of(message));

            if (temperature != null) {
                requestBody.put("temperature", temperature);
            } else if (llmConfig.getOpenai().getTemperature() > 0) {
                requestBody.put("temperature", llmConfig.getOpenai().getTemperature());
            }

            // Call OpenAI API
            WebClient webClient = webClientBuilder
                .baseUrl(llmConfig.getOpenai().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + llmConfig.getOpenai().getApiKey())
                .defaultHeader("content-type", "application/json")
                .build();

            log.info("Calling OpenAI API with model: {}", requestBody.get("model"));

            Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // Parse response
            String responseText = extractOpenAiResponse(response);
            int tokensUsed = extractTokenCount(response);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("OpenAI API call completed in {}ms, tokens used: {}", responseTime, tokensUsed);

            return new LlmResponse(
                "openai",
                model != null ? model : llmConfig.getOpenai().getModel(),
                responseText,
                tokensUsed,
                responseTime
            );
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            throw new LlmException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    private String extractOpenAiResponse(Map<String, Object> response) {
        // Extract text from response.choices[0].message.content
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            if (message != null) {
                return (String) message.get("content");
            }
        }
        return "";
    }

    private int extractTokenCount(Map<String, Object> response) {
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            Integer completion = (Integer) usage.get("completion_tokens");
            return completion != null ? completion : 0;
        }
        return 0;
    }
}
