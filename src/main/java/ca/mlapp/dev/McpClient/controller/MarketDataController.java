package ca.mlapp.dev.McpClient.controller;

import ca.mlapp.dev.McpClient.service.McpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Slf4j
public class
MarketDataController {

    private final McpClientService mcpService;

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<Object> getQuote(@PathVariable String symbol) {
        log.info("GET /api/market/quote/{}", symbol);
        Object result = mcpService.fetchRealtimeQuote(symbol);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/historical/{symbol}")
    public ResponseEntity<Object> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam String interval,
            @RequestParam(required = false) Integer outputSize) {
        log.info("GET /api/market/historical/{}?interval={}&outputSize={}", symbol, interval, outputSize);
        Object result = mcpService.getHistoricalData(symbol, interval, outputSize);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh-prices")
    public ResponseEntity<Object> refreshAllPrices() {
        log.info("POST /api/market/refresh-prices");
        Object result = mcpService.refreshAllPrices();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchSymbols(@RequestParam String query) {
        log.info("GET /api/market/search?query={}", query);
        Object result = mcpService.searchStockSymbols(query);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api-usage")
    public ResponseEntity<Object> getApiUsage() {
        log.info("GET /api/market/api-usage");
        Object result = mcpService.getApiUsage();
        return ResponseEntity.ok(result);
    }

    // ==================== Technical Indicator Endpoints ====================

    @GetMapping("/indicators/sma/{symbol}")
    public ResponseEntity<Object> getSMA(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer timePeriod,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String seriesType) {
        log.info("GET /api/market/indicators/sma/{} with timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Object result = mcpService.getSMA(symbol, timePeriod, interval, seriesType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/indicators/ema/{symbol}")
    public ResponseEntity<Object> getEMA(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer timePeriod,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String seriesType) {
        log.info("GET /api/market/indicators/ema/{} with timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Object result = mcpService.getEMA(symbol, timePeriod, interval, seriesType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/indicators/rsi/{symbol}")
    public ResponseEntity<Object> getRSI(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer timePeriod,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String seriesType) {
        log.info("GET /api/market/indicators/rsi/{} with timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Object result = mcpService.getRSI(symbol, timePeriod, interval, seriesType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/indicators/macd/{symbol}")
    public ResponseEntity<Object> getMACD(
            @PathVariable String symbol,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String seriesType) {
        log.info("GET /api/market/indicators/macd/{} with interval={}, seriesType={}",
                symbol, interval, seriesType);
        Object result = mcpService.getMACD(symbol, interval, seriesType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/indicators/bbands/{symbol}")
    public ResponseEntity<Object> getBollingerBands(
            @PathVariable String symbol,
            @RequestParam(required = false) Integer timePeriod,
            @RequestParam(required = false) String interval,
            @RequestParam(required = false) String seriesType) {
        log.info("GET /api/market/indicators/bbands/{} with timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Object result = mcpService.getBollingerBands(symbol, timePeriod, interval, seriesType);
        return ResponseEntity.ok(result);
    }
}
