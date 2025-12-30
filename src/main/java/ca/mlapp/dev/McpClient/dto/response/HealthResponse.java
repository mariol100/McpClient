package ca.mlapp.dev.McpClient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String message;
    private boolean mcpClientConnected;
    private int toolsCount;
    private int promptsCount;
    private int resourcesCount;
}
