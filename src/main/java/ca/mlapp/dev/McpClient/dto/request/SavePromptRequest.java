package ca.mlapp.dev.McpClient.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePromptRequest {
    private String promptType;
    private String prompt;
    private String provider;
    private String model;
    private String response;
    private Integer tokensUsed;
    private Long responseTimeMs;
    private Map<String, Object> inputParameters;
}
