package ca.mlapp.dev.McpClient.service.llm;

import ca.mlapp.dev.McpClient.config.LlmConfig;
import ca.mlapp.dev.McpClient.dto.llm.LlmRequest;
import ca.mlapp.dev.McpClient.dto.llm.LlmResponse;
import ca.mlapp.dev.McpClient.exception.LlmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmServiceImpl implements LlmService {
    private final LlmConfig llmConfig;
    private final AnthropicLlmService anthropicService;
    private final OpenAiLlmService openAiService;
    private final OllamaLlmService ollamaService;

    @Override
    public LlmResponse sendPrompt(String prompt, String provider) {
        LlmRequest request = new LlmRequest();
        request.setProvider(provider != null ? provider : llmConfig.getDefaultProvider());
        request.setPrompt(prompt);
        return sendPrompt(request);
    }

    @Override
    public LlmResponse sendPrompt(LlmRequest request) {
        String provider = request.getProvider() != null ?
            request.getProvider() : llmConfig.getDefaultProvider();

        log.info("Sending prompt to {} (length: {} chars)", provider, request.getPrompt().length());

        return switch (provider.toLowerCase()) {
            case "claude", "anthropic" -> anthropicService.sendPrompt(
                request.getPrompt(),
                request.getModel(),
                request.getMaxTokens(),
                request.getTemperature()
            );
            case "openai", "gpt" -> openAiService.sendPrompt(
                request.getPrompt(),
                request.getModel(),
                request.getMaxTokens(),
                request.getTemperature()
            );
            case "ollama" -> ollamaService.sendPrompt(
                request.getPrompt(),
                request.getModel()
            );
            default -> throw new LlmException("Unsupported provider: " + provider);
        };
    }

    @Override
    public boolean isProviderAvailable(String provider) {
        if (provider == null) {
            return false;
        }

        return switch (provider.toLowerCase()) {
            case "claude", "anthropic" ->
                llmConfig.getAnthropic() != null &&
                llmConfig.getAnthropic().getApiKey() != null &&
                !llmConfig.getAnthropic().getApiKey().equals("your-api-key-here");
            case "openai", "gpt" ->
                llmConfig.getOpenai() != null &&
                llmConfig.getOpenai().getApiKey() != null &&
                !llmConfig.getOpenai().getApiKey().equals("your-api-key-here");
            case "ollama" ->
                llmConfig.getOllama() != null &&
                llmConfig.getOllama().isEnabled();
            default -> false;
        };
    }

    @Override
    public List<String> getAvailableProviders() {
        List<String> providers = new ArrayList<>();
        if (isProviderAvailable("claude")) providers.add("claude");
        if (isProviderAvailable("openai")) providers.add("openai");
        if (isProviderAvailable("ollama")) providers.add("ollama");
        return providers;
    }
}
