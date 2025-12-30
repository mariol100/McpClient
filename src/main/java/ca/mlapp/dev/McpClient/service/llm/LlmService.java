package ca.mlapp.dev.McpClient.service.llm;

import ca.mlapp.dev.McpClient.dto.llm.LlmRequest;
import ca.mlapp.dev.McpClient.dto.llm.LlmResponse;
import ca.mlapp.dev.McpClient.exception.LlmException;

import java.util.List;

public interface LlmService {
    /**
     * Send a prompt to the LLM and get a response
     */
    LlmResponse sendPrompt(String prompt, String provider) throws LlmException;

    /**
     * Send a prompt with custom parameters
     */
    LlmResponse sendPrompt(LlmRequest request) throws LlmException;

    /**
     * Check if a provider is available/configured
     */
    boolean isProviderAvailable(String provider);

    /**
     * Get list of available providers
     */
    List<String> getAvailableProviders();
}
