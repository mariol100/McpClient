package ca.mlapp.dev.McpClient.dto.twelvedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymbolSearchResultDTO {
    private String symbol;

    @JsonProperty("instrument_name")
    private String instrumentName;

    private String exchange;

    @JsonProperty("mic_code")
    private String micCode;

    @JsonProperty("exchange_timezone")
    private String exchangeTimezone;

    @JsonProperty("instrument_type")
    private String instrumentType;

    private String country;
    private String currency;
}
