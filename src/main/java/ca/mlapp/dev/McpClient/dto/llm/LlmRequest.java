package ca.mlapp.dev.McpClient.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    private String provider;      // "claude", "openai", "ollama"
    private String prompt;         // The actual prompt text
    private String model;          // Optional: override default model
    private Integer maxTokens;     // Optional: override default
    private Double temperature;    // Optional: override default
}
