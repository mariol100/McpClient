package ca.mlapp.dev.McpClient.controller;

import ca.mlapp.dev.McpClient.dto.llm.LlmRequest;
import ca.mlapp.dev.McpClient.dto.llm.LlmResponse;
import ca.mlapp.dev.McpClient.dto.request.SavePromptRequest;
import ca.mlapp.dev.McpClient.dto.response.PromptResult;
import ca.mlapp.dev.McpClient.dto.response.SavePromptResponse;
import ca.mlapp.dev.McpClient.entity.PromptHistory;
import ca.mlapp.dev.McpClient.exception.LlmException;
import ca.mlapp.dev.McpClient.service.McpClientService;
import ca.mlapp.dev.McpClient.service.PromptHistoryService;
import ca.mlapp.dev.McpClient.service.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@Slf4j
public class PromptsController {

    private final McpClientService mcpService;
    private final LlmService llmService;
    private final PromptHistoryService promptHistoryService;

    @GetMapping("/stock-analysis/{symbol}")
    public ResponseEntity<PromptResult> getStockAnalysis(@PathVariable String symbol) {
        log.info("GET /api/prompts/stock-analysis/{}", symbol);
        String content = mcpService.getStockAnalysisPrompt(symbol);
        PromptResult result = new PromptResult("stock-analysis-prompt", content);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/portfolio-review")
    public ResponseEntity<PromptResult> getPortfolioReview(
            @RequestParam(required = false) String focus) {
        log.info("GET /api/prompts/portfolio-review?focus={}", focus);
        String content = mcpService.getPortfolioReviewPrompt(focus);
        PromptResult result = new PromptResult("portfolio-review-prompt", content);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/investment-advice")
    public ResponseEntity<PromptResult> getInvestmentAdvice(
            @RequestParam Double amount,
            @RequestParam(required = false) String riskTolerance) {
        log.info("GET /api/prompts/investment-advice?amount={}&riskTolerance={}", amount, riskTolerance);
        String content = mcpService.getInvestmentAdvicePrompt(amount, riskTolerance);
        PromptResult result = new PromptResult("investment-advice-prompt", content);
        return ResponseEntity.ok(result);
    }

    // ==================== LLM Integration Endpoints ====================

    @PostMapping("/generate-ai-response")
    public ResponseEntity<LlmResponse> generateAiResponse(@RequestBody LlmRequest request) {
        log.info("POST /api/prompts/generate-ai-response - provider: {}", request.getProvider());

        if (!llmService.isProviderAvailable(request.getProvider())) {
            throw new LlmException("Provider not available or not configured: " + request.getProvider());
        }

        LlmResponse response = llmService.sendPrompt(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-providers")
    public ResponseEntity<List<String>> getAvailableProviders() {
        log.info("GET /api/prompts/available-providers");
        List<String> providers = llmService.getAvailableProviders();
        return ResponseEntity.ok(providers);
    }

    // ==================== Prompt History Endpoints ====================

    @PostMapping("/save")
    public ResponseEntity<SavePromptResponse> savePromptResponse(@RequestBody SavePromptRequest request) {
        log.info("POST /api/prompts/save - type: {}, provider: {}",
                 request.getPromptType(), request.getProvider());

        SavePromptResponse response = promptHistoryService.savePromptResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PromptHistory>> getHistory(
            @RequestParam(required = false) String promptType) {
        log.info("GET /api/prompts/history - promptType: {}", promptType);

        List<PromptHistory> history = promptType != null
            ? promptHistoryService.getHistoryByType(promptType)
            : promptHistoryService.getAllHistory();

        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/paginated")
    public ResponseEntity<Map<String, Object>> getHistoryPaginated(
            @RequestParam(required = false) String promptType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "desc") String sort) {

        log.info("GET /api/prompts/history/paginated - page: {}, size: {}, sort: {}", page, size, sort);

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "timestamp"));

        Page<PromptHistory> historyPage = promptType != null
            ? promptHistoryService.getHistoryByTypePaginated(promptType, pageable)
            : promptHistoryService.getAllHistoryPaginated(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", historyPage.getContent());
        response.put("currentPage", historyPage.getNumber());
        response.put("totalItems", historyPage.getTotalElements());
        response.put("totalPages", historyPage.getTotalPages());
        response.put("pageSize", historyPage.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<PromptHistory> getHistoryById(@PathVariable Long id) {
        log.info("GET /api/prompts/history/{}", id);
        PromptHistory history = promptHistoryService.getHistoryById(id);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        log.info("DELETE /api/prompts/history/{}", id);
        promptHistoryService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }
}
