package ca.mlapp.dev.McpClient.controller;

import ca.mlapp.dev.McpClient.dto.response.ResourceContent;
import ca.mlapp.dev.McpClient.service.McpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourcesController {

    private final McpClientService mcpService;

    @GetMapping("/stock/{symbol}")
    public ResponseEntity<ResourceContent> getStockResource(@PathVariable String symbol) {
        log.info("GET /api/resources/stock/{}", symbol);
        String content = mcpService.getStockResource(symbol);
        ResourceContent result = new ResourceContent("stock://" + symbol, content);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/portfolio/summary")
    public ResponseEntity<ResourceContent> getPortfolioSummary() {
        log.info("GET /api/resources/portfolio/summary");
        String content = mcpService.getPortfolioSummary();
        ResourceContent result = new ResourceContent("portfolio://summary", content);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/portfolio/list")
    public ResponseEntity<ResourceContent> getStockList() {
        log.info("GET /api/resources/portfolio/list");
        String content = mcpService.getStockList();
        ResourceContent result = new ResourceContent("portfolio://list", content);
        return ResponseEntity.ok(result);
    }
}
