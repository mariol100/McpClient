package ca.mlapp.dev.McpClient.controller;

import ca.mlapp.dev.McpClient.dto.response.HealthResponse;
import ca.mlapp.dev.McpClient.service.McpClientService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@Slf4j
public class MetadataController {

    private final McpClientService mcpService;

    @GetMapping("/tools")
    public ResponseEntity<List<McpSchema.Tool>> listTools() {
        log.info("GET /api/metadata/tools");
        List<McpSchema.Tool> tools = mcpService.listAllTools();
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/prompts")
    public ResponseEntity<List<McpSchema.Prompt>> listPrompts() {
        log.info("GET /api/metadata/prompts");
        List<McpSchema.Prompt> prompts = mcpService.listAllPrompts();
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/resources")
    public ResponseEntity<List<McpSchema.Resource>> listResources() {
        log.info("GET /api/metadata/resources");
        List<McpSchema.Resource> resources = mcpService.listAllResources();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        log.info("GET /api/metadata/health");
        try {
            List<McpSchema.Tool> tools = mcpService.listAllTools();
            List<McpSchema.Prompt> prompts = mcpService.listAllPrompts();
            List<McpSchema.Resource> resources = mcpService.listAllResources();

            HealthResponse health = new HealthResponse(
                "UP",
                "MCP Client is connected and operational",
                true,
                tools.size(),
                prompts.size(),
                resources.size()
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            HealthResponse health = new HealthResponse(
                "DOWN",
                "MCP Client connection failed: " + e.getMessage(),
                false,
                0,
                0,
                0
            );
            return ResponseEntity.status(503).body(health);
        }
    }
}
