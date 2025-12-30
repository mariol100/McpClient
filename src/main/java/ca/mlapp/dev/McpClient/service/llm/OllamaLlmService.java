package ca.mlapp.dev.McpClient.service.llm;

import ca.mlapp.dev.McpClient.config.LlmConfig;
import ca.mlapp.dev.McpClient.dto.llm.LlmResponse;
import ca.mlapp.dev.McpClient.exception.LlmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaLlmService {
    private final LlmConfig llmConfig;
    private final WebClient.Builder webClientBuilder;

    public LlmResponse sendPrompt(String prompt, String model) {
        long startTime = System.currentTimeMillis();

        try {
            // Build request body for Ollama API
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model != null ? model : llmConfig.getOllama().getModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            // Call Ollama API
            WebClient webClient = webClientBuilder
                .baseUrl(llmConfig.getOllama().getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .build();

            log.info("Calling Ollama API with model: {}", requestBody.get("model"));

            Map<String, Object> response = webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // Parse response
            String responseText = extractOllamaResponse(response);
            long responseTime = System.currentTimeMillis() - startTime;

            log.info("Ollama API call completed in {}ms", responseTime);

            return new LlmResponse(
                "ollama",
                model != null ? model : llmConfig.getOllama().getModel(),
                responseText,
                0, // Ollama doesn't return token count in the same way
                responseTime
            );
        } catch (Exception e) {
            log.error("Error calling Ollama API: {}", e.getMessage(), e);
            throw new LlmException("Failed to call Ollama API: " + e.getMessage(), e);
        }
    }

    private String extractOllamaResponse(Map<String, Object> response) {
        // Extract text from response.response
        Object responseText = response.get("response");
        return responseText != null ? responseText.toString() : "";
    }
}
