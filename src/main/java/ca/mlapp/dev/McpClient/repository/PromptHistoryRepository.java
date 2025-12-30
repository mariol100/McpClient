package ca.mlapp.dev.McpClient.repository;

import ca.mlapp.dev.McpClient.entity.PromptHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromptHistoryRepository extends JpaRepository<PromptHistory, Long> {

    List<PromptHistory> findAllByOrderByTimestampDesc();

    List<PromptHistory> findByPromptTypeOrderByTimestampDesc(String promptType);

    List<PromptHistory> findByProviderOrderByTimestampDesc(String provider);

    List<PromptHistory> findByTimestampAfterOrderByTimestampDesc(LocalDateTime after);

    Page<PromptHistory> findByPromptType(String promptType, Pageable pageable);
}
