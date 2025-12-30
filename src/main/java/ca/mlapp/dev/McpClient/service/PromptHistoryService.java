package ca.mlapp.dev.McpClient.service;

import ca.mlapp.dev.McpClient.dto.request.SavePromptRequest;
import ca.mlapp.dev.McpClient.dto.response.SavePromptResponse;
import ca.mlapp.dev.McpClient.entity.PromptHistory;
import ca.mlapp.dev.McpClient.repository.PromptHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptHistoryService {

    private final PromptHistoryRepository repository;

    @Transactional
    public SavePromptResponse savePromptResponse(SavePromptRequest request) {
        log.info("Saving prompt history: type={}, provider={}, model={}",
                 request.getPromptType(), request.getProvider(), request.getModel());

        validateRequest(request);

        PromptHistory history = new PromptHistory();
        history.setPromptType(request.getPromptType());
        history.setPrompt(request.getPrompt());
        history.setProvider(request.getProvider());
        history.setModel(request.getModel());
        history.setResponse(request.getResponse());
        history.setTokensUsed(request.getTokensUsed());
        history.setResponseTimeMs(request.getResponseTimeMs());
        history.setInputParameters(request.getInputParameters());

        PromptHistory saved = repository.save(history);

        log.info("Prompt saved successfully with ID: {}", saved.getId());

        return new SavePromptResponse(saved.getId(), saved.getTimestamp());
    }

    public List<PromptHistory> getAllHistory() {
        return repository.findAllByOrderByTimestampDesc();
    }

    public List<PromptHistory> getHistoryByType(String promptType) {
        return repository.findByPromptTypeOrderByTimestampDesc(promptType);
    }

    public Page<PromptHistory> getAllHistoryPaginated(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<PromptHistory> getHistoryByTypePaginated(String promptType, Pageable pageable) {
        return repository.findByPromptType(promptType, pageable);
    }

    public PromptHistory getHistoryById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("History record not found: " + id));
    }

    public void deleteHistory(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("History record not found: " + id);
        }
        repository.deleteById(id);
    }

    private void validateRequest(SavePromptRequest request) {
        if (request.getPromptType() == null || request.getPromptType().isBlank()) {
            throw new IllegalArgumentException("Prompt type is required");
        }
        if (request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw new IllegalArgumentException("Prompt is required");
        }
        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new IllegalArgumentException("Provider is required");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (request.getResponse() == null || request.getResponse().isBlank()) {
            throw new IllegalArgumentException("Response is required");
        }
    }
}
