package ca.mlapp.dev.McpClient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptResult {
    private String promptName;
    private String content;
}
