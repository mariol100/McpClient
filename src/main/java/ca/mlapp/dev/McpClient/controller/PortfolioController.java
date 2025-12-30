package ca.mlapp.dev.McpClient.controller;

import ca.mlapp.dev.McpClient.dto.request.AddStockRequest;
import ca.mlapp.dev.McpClient.dto.request.UpdatePriceRequest;
import ca.mlapp.dev.McpClient.dto.request.UpdateSharesRequest;
import ca.mlapp.dev.McpClient.service.McpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final McpClientService mcpService;

    @GetMapping("/stocks")
    public ResponseEntity<Object> getAllStocks() {
        log.info("GET /api/portfolio/stocks");
        Object result = mcpService.listAllStocks();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<Object> getStock(@PathVariable String symbol) {
        log.info("GET /api/portfolio/stocks/{}", symbol);
        Object result = mcpService.getStock(symbol);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stocks")
    public ResponseEntity<Object> addStock(@RequestBody AddStockRequest request) {
        log.info("POST /api/portfolio/stocks - {}", request);
        Object result = mcpService.addStock(
            request.getSymbol(),
            request.getName(),
            request.getPrice(),
            request.getShares()
        );
        return ResponseEntity.ok(result);
    }

    @PutMapping("/stocks/{symbol}/price")
    public ResponseEntity<Object> updatePrice(
            @PathVariable String symbol,
            @RequestBody UpdatePriceRequest request) {
        log.info("PUT /api/portfolio/stocks/{}/price - {}", symbol, request);
        Object result = mcpService.updateStockPrice(symbol, request.getNewPrice());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/stocks/{symbol}/shares")
    public ResponseEntity<Object> updateShares(
            @PathVariable String symbol,
            @RequestBody UpdateSharesRequest request) {
        log.info("PUT /api/portfolio/stocks/{}/shares - {}", symbol, request);
        Object result = mcpService.updateStockShares(symbol, request.getNewShares());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/stocks/{symbol}")
    public ResponseEntity<Void> deleteStock(@PathVariable String symbol) {
        log.info("DELETE /api/portfolio/stocks/{}", symbol);
        mcpService.deleteStock(symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stocks/search")
    public ResponseEntity<Object> searchStocks(@RequestParam String pattern) {
        log.info("GET /api/portfolio/stocks/search?pattern={}", pattern);
        Object result = mcpService.searchStocks(pattern);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/value")
    public ResponseEntity<Object> getPortfolioValue() {
        log.info("GET /api/portfolio/value");
        Object result = mcpService.calculatePortfolioValue();
        return ResponseEntity.ok(result);
    }
}
