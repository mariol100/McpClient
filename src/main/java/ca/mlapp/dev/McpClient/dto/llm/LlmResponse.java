package ca.mlapp.dev.McpClient.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    private String provider;
    private String model;
    private String response;
    private int tokensUsed;
    private long responseTimeMs;
}
