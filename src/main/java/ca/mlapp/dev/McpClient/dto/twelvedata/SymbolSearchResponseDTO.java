package ca.mlapp.dev.McpClient.dto.twelvedata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymbolSearchResponseDTO {
    private List<SymbolSearchResultDTO> data;
    private String status;
}
