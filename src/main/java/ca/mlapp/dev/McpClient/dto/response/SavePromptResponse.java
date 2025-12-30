package ca.mlapp.dev.McpClient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavePromptResponse {
    private Long id;
    private LocalDateTime timestamp;
    private String message;

    public SavePromptResponse(Long id, LocalDateTime timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.message = "Prompt saved successfully";
    }
}
