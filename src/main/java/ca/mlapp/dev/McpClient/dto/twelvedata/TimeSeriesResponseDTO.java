package ca.mlapp.dev.McpClient.dto.twelvedata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesResponseDTO {
    private MetaDTO meta;
    private List<TimeSeriesValueDTO> values;
    private String status;
}
