package ca.mlapp.dev.McpClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
        log.info("MCP Client Web Application started successfully!");
        log.info("Access the application at: http://localhost:8090");
    }
}
