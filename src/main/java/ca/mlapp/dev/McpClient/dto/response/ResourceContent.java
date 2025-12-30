package ca.mlapp.dev.McpClient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceContent {
    private String uri;
    private String content;
}
