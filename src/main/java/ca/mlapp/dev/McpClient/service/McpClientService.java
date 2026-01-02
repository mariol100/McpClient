package ca.mlapp.dev.McpClient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpClientService {

    private final List<McpSyncClient> mcpClients;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private McpSyncClient getClient() {
        if (mcpClients == null || mcpClients.isEmpty()) {
            throw new IllegalStateException("No MCP clients configured");
        }
        return mcpClients.get(0);
    }

    // ==================== Discovery Methods ====================

    public List<McpSchema.Tool> listAllTools() {
        log.debug("Listing all available tools");
        McpSchema.ListToolsResult result = getClient().listTools();
        return result.tools();
    }

    public List<McpSchema.Prompt> listAllPrompts() {
        log.debug("Listing all available prompts");
        McpSchema.ListPromptsResult result = getClient().listPrompts();
        return result.prompts();
    }

    public List<McpSchema.Resource> listAllResources() {
        log.debug("Listing all available resources");
        McpSchema.ListResourcesResult result = getClient().listResources();
        return result.resources();
    }

    // ==================== Portfolio Tools ====================

    public Object listAllStocks() {
        log.debug("Calling tool: list-all-stocks");
        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("list-all-stocks", Map.of())
        );
        return parseToolResult(result);
    }

    public Object getStock(String symbol) {
        log.debug("Calling tool: get-stock with symbol={}", symbol);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-stock", args)
        );
        return parseToolResult(result);
    }

    public Object addStock(String symbol, String name, Double price, Integer shares) {
        log.debug("Calling tool: add-stock with symbol={}, name={}, price={}, shares={}",
                  symbol, name, price, shares);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (name != null) args.put("name", name);
        if (price != null) args.put("price", price);
        if (shares != null) args.put("shares", shares);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("add-stock", args)
        );
        return parseToolResult(result);
    }

    public Object updateStockPrice(String symbol, Double newPrice) {
        log.debug("Calling tool: update-stock-price with symbol={}, newPrice={}", symbol, newPrice);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        args.put("newPrice", newPrice);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("update-stock-price", args)
        );
        return parseToolResult(result);
    }

    public Object updateStockShares(String symbol, Integer newShares) {
        log.debug("Calling tool: update-stock-shares with symbol={}, newShares={}", symbol, newShares);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        args.put("newShares", newShares);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("update-stock-shares", args)
        );
        return parseToolResult(result);
    }

    public void deleteStock(String symbol) {
        log.debug("Calling tool: delete-stock with symbol={}", symbol);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("delete-stock", args)
        );
        parseToolResult(result);
    }

    public Object searchStocks(String pattern) {
        log.debug("Calling tool: search-stocks with pattern={}", pattern);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("pattern", pattern);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("search-stocks", args)
        );
        return parseToolResult(result);
    }

    public Object calculatePortfolioValue() {
        log.debug("Calling tool: calculate-portfolio-value");
        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("calculate-portfolio-value", Map.of())
        );
        return parseToolResult(result);
    }

    // ==================== TwelveData Tools ====================

    public Object fetchRealtimeQuote(String symbol) {
        log.debug("Calling tool: fetch-realtime-quote with symbol={}", symbol);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("fetch-realtime-quote", args)
        );
        return parseToolResult(result);
    }

    public Object getHistoricalData(String symbol, String interval, Integer outputSize) {
        log.debug("Calling tool: get-historical-data with symbol={}, interval={}, outputSize={}",
                  symbol, interval, outputSize);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        args.put("interval", interval);
        if (outputSize != null) args.put("outputSize", outputSize);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-historical-data", args)
        );
        return parseToolResult(result);
    }

    public Object refreshAllPrices() {
        log.debug("Calling tool: refresh-all-prices");
        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("refresh-all-prices", Map.of())
        );
        return parseToolResult(result);
    }

    public Object searchStockSymbols(String query) {
        log.debug("Calling tool: search-stock-symbols with query={}", query);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("query", query);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("search-stock-symbols", args)
        );

        // Parse the raw result (returns List<SymbolSearchResultDTO>)
        Object rawResult = parseToolResult(result);

        // Wrap in the format expected by frontend: {data: [...], status: "ok"}
        Map<String, Object> wrappedResult = new LinkedHashMap<>();
        wrappedResult.put("data", rawResult);
        wrappedResult.put("status", "ok");

        return wrappedResult;
    }

    public Object getApiUsage() {
        log.debug("Calling tool: get-api-usage");
        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-api-usage", Map.of())
        );
        return parseToolResult(result);
    }

    // ==================== Technical Indicator Methods ====================

    public Object getSMA(String symbol, Integer timePeriod, String interval, String seriesType) {
        log.debug("Calling tool: get-sma with symbol={}, timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (timePeriod != null) args.put("timePeriod", timePeriod);
        if (interval != null) args.put("interval", interval);
        if (seriesType != null) args.put("seriesType", seriesType);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-sma", args)
        );
        return parseToolResult(result);
    }

    public Object getEMA(String symbol, Integer timePeriod, String interval, String seriesType) {
        log.debug("Calling tool: get-ema with symbol={}, timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (timePeriod != null) args.put("timePeriod", timePeriod);
        if (interval != null) args.put("interval", interval);
        if (seriesType != null) args.put("seriesType", seriesType);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-ema", args)
        );
        return parseToolResult(result);
    }

    public Object getRSI(String symbol, Integer timePeriod, String interval, String seriesType) {
        log.debug("Calling tool: get-rsi with symbol={}, timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (timePeriod != null) args.put("timePeriod", timePeriod);
        if (interval != null) args.put("interval", interval);
        if (seriesType != null) args.put("seriesType", seriesType);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-rsi", args)
        );
        return parseToolResult(result);
    }

    public Object getMACD(String symbol, String interval, String seriesType) {
        log.debug("Calling tool: get-macd with symbol={}, interval={}, seriesType={}",
                symbol, interval, seriesType);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (interval != null) args.put("interval", interval);
        if (seriesType != null) args.put("seriesType", seriesType);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-macd", args)
        );
        return parseToolResult(result);
    }

    public Object getBollingerBands(String symbol, Integer timePeriod, String interval, String seriesType) {
        log.debug("Calling tool: get-bbands with symbol={}, timePeriod={}, interval={}, seriesType={}",
                symbol, timePeriod, interval, seriesType);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);
        if (timePeriod != null) args.put("timePeriod", timePeriod);
        if (interval != null) args.put("interval", interval);
        if (seriesType != null) args.put("seriesType", seriesType);

        McpSchema.CallToolResult result = getClient().callTool(
            new McpSchema.CallToolRequest("get-bbands", args)
        );
        return parseToolResult(result);
    }

    // ==================== Prompt Methods ====================

    public String getStockAnalysisPrompt(String symbol) {
        log.debug("Getting stock-analysis with symbol={}", symbol);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("symbol", symbol);

        McpSchema.GetPromptResult result = getClient().getPrompt(
            new McpSchema.GetPromptRequest("stock-analysis", args)
        );
        return extractPromptText(result);
    }

    public String getPortfolioReviewPrompt(String focus) {
        log.debug("Getting portfolio-review with focus={}", focus);
        Map<String, Object> args = new LinkedHashMap<>();
        if (focus != null && !focus.isBlank()) {
            args.put("focus", focus);
        }

        McpSchema.GetPromptResult result = getClient().getPrompt(
            new McpSchema.GetPromptRequest("portfolio-review", args)
        );
        return extractPromptText(result);
    }

    public String getInvestmentAdvicePrompt(Double amount, String riskTolerance) {
        log.debug("Getting investment-advice with amount={}, riskTolerance={}",
                  amount, riskTolerance);
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("amount", amount);
        if (riskTolerance != null && !riskTolerance.isBlank()) {
            args.put("riskTolerance", riskTolerance);
        }

        McpSchema.GetPromptResult result = getClient().getPrompt(
            new McpSchema.GetPromptRequest("investment-advice", args)
        );
        return extractPromptText(result);
    }

    // ==================== Resource Methods ====================

    public String getStockResource(String symbol) {
        log.debug("Reading stock resource with symbol={}", symbol);
        String uri = "stock://" + symbol;
        McpSchema.ReadResourceResult result = getClient().readResource(
            new McpSchema.ReadResourceRequest(uri)
        );
        return extractResourceText(result);
    }

    public String getPortfolioSummary() {
        log.debug("Reading portfolio://summary resource");
        McpSchema.ReadResourceResult result = getClient().readResource(
            new McpSchema.ReadResourceRequest("portfolio://summary")
        );
        return extractResourceText(result);
    }

    public String getStockList() {
        log.debug("Reading portfolio://list resource");
        McpSchema.ReadResourceResult result = getClient().readResource(
            new McpSchema.ReadResourceRequest("portfolio://list")
        );
        return extractResourceText(result);
    }

    // ==================== Helper Methods ====================

    private Object parseToolResult(McpSchema.CallToolResult result) {
        if (result.isError()) {
            String errorMsg = "Tool call failed";
            if (result.content() != null && !result.content().isEmpty()) {
                errorMsg = result.content().toString();
            }
            log.error("Tool call error: {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (result.content() == null || result.content().isEmpty()) {
            return null;
        }

        // Extract text content and parse as JSON
        for (var content : result.content()) {
            if (content instanceof McpSchema.TextContent textContent) {
                String text = textContent.text();
                try {
                    // Try to parse as JSON
                    return objectMapper.readValue(text, Object.class);
                } catch (Exception e) {
                    // If not JSON, return as string
                    return text;
                }
            }
        }

        return null;
    }

    private String extractPromptText(McpSchema.GetPromptResult result) {
        if (result.messages() == null || result.messages().isEmpty()) {
            return "";
        }

        StringBuilder promptText = new StringBuilder();
        for (var message : result.messages()) {
            if (message.content() instanceof McpSchema.TextContent textContent) {
                promptText.append(textContent.text()).append("\n");
            }
        }

        return promptText.toString().trim();
    }

    private String extractResourceText(McpSchema.ReadResourceResult result) {
        if (result.contents() == null || result.contents().isEmpty()) {
            return "";
        }

        StringBuilder resourceText = new StringBuilder();
        for (var content : result.contents()) {
            if (content instanceof McpSchema.TextResourceContents textContent) {
                resourceText.append(textContent.text()).append("\n");
            }
        }

        return resourceText.toString().trim();
    }
}
