package ca.mlapp.dev.McpClient.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDTO {
    private String symbol;
    private String name;
    private String exchange;
    private String currency;
    private String datetime;
    private Long timestamp;

    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;

    @JsonProperty("previous_close")
    private String previousClose;

    private String change;

    @JsonProperty("percent_change")
    private String percentChange;

    @JsonProperty("average_volume")
    private String averageVolume;

    @JsonProperty("is_market_open")
    private Boolean isMarketOpen;

    @JsonProperty("fifty_two_week")
    private FiftyTwoWeekDTO fiftyTwoWeek;

    public Double getCloseAsDouble() {
        try {
            return close != null ? Double.parseDouble(close) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
