package ca.mlapp.dev.McpClient;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class McpClientApplication implements CommandLineRunner {

    private final List<McpSyncClient> mcpClients;

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("=".repeat(60));
        log.info("MCP Client Starting...");
        log.info("=".repeat(60));

        if (mcpClients.isEmpty()) {
            log.error("No MCP clients found! Check your configuration.");
            return;
        }

        try {
            for (McpSyncClient client : mcpClients) {
                testMcpClient(client);
            }
        } catch (Exception e) {
            log.error("Error during MCP client test: {}", e.getMessage(), e);
        }
    }

    private void testMcpClient(McpSyncClient client) {
        log.info("\n" + "=".repeat(60));
        log.info("Testing MCP Client");
        log.info("=".repeat(60));

        // List all tools
        log.info("\n" + "-".repeat(60));
        log.info("Listing all available tools...");
        log.info("-".repeat(60));
        McpSchema.ListToolsResult toolsResult = client.listTools();
        log.info("Found {} tools:", toolsResult.tools().size());
        toolsResult.tools().forEach(tool ->
            log.info("  - {} : {}", tool.name(), tool.description())
        );

        // Test a simple tool call - list all stocks
        log.info("\n" + "-".repeat(60));
        log.info("Testing tool: list-all-stocks");
        log.info("-".repeat(60));
        try {
            McpSchema.CallToolResult result = client.callTool(
                new McpSchema.CallToolRequest("list-all-stocks", Map.of())
            );

            if (!result.isError()) {
                log.info("Success! Result:");
                result.content().forEach(content -> {
                    if (content instanceof McpSchema.TextContent textContent) {
                        log.info("{}", textContent.text());
                    }
                });
            } else {
                log.error("Tool call failed: {}", result.content());
            }
        } catch (Exception e) {
            log.error("Error calling tool: {}", e.getMessage());
        }

        // List all prompts
        log.info("\n" + "-".repeat(60));
        log.info("Listing all available prompts...");
        log.info("-".repeat(60));
        McpSchema.ListPromptsResult promptsResult = client.listPrompts();
        log.info("Found {} prompts:", promptsResult.prompts().size());
        promptsResult.prompts().forEach(prompt ->
            log.info("  - {} : {}", prompt.name(), prompt.description())
        );

        // List all resources
        log.info("\n" + "-".repeat(60));
        log.info("Listing all available resources...");
        log.info("-".repeat(60));
        McpSchema.ListResourcesResult resourcesResult = client.listResources();
        log.info("Found {} resources:", resourcesResult.resources().size());
        resourcesResult.resources().forEach(resource ->
            log.info("  - {} : {}", resource.name(), resource.description())
        );

        log.info("\n" + "=".repeat(60));
        log.info("MCP Client test completed successfully!");
        log.info("=".repeat(60));
    }
}
