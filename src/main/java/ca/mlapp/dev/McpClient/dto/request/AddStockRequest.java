package ca.mlapp.dev.McpClient.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStockRequest {
    private String symbol;
    private String name;
    private Double price;
    private Integer shares;
}
