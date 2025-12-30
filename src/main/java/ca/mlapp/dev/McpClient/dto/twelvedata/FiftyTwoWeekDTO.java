package ca.mlapp.dev.McpClient.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiftyTwoWeekDTO {
    private String low;
    private String high;

    @JsonProperty("low_change")
    private String lowChange;

    @JsonProperty("high_change")
    private String highChange;

    @JsonProperty("low_change_percent")
    private String lowChangePercent;

    @JsonProperty("high_change_percent")
    private String highChangePercent;

    private String range;
}
