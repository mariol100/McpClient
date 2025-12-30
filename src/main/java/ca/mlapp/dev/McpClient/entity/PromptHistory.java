package ca.mlapp.dev.McpClient.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "prompt_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 50)
    private String promptType;  // 'stock-analysis', 'portfolio-review', 'investment-advice'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(nullable = false, length = 50)
    private String provider;  // 'anthropic', 'openai', 'ollama'

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    private Integer tokensUsed;

    private Long responseTimeMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> inputParameters;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
