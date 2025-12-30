package ca.mlapp.dev.McpClient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {

    private Long id;
    private String symbol;
    private String name;
    private Double currentPrice;
    private Integer shares;
    private Double totalValue;
    private String lastUpdated;
}
