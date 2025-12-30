package ca.mlapp.dev.McpClient.dto.twelvedata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwelveDataErrorDTO {
    private String code;
    private String message;
    private String status;
}
