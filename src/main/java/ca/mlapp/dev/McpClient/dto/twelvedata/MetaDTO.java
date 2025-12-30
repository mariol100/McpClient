package ca.mlapp.dev.McpClient.dto.twelvedata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDTO {
    private String symbol;
    private String interval;
    private String currency;
    private String exchange;
    private String type;
}
