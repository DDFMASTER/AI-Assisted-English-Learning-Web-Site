package Entities;

import java.time.LocalDateTime;

public class AiWordVote {
    private Long userId;
    private Long aidicId;
    private Integer action;
    private LocalDateTime timestamp;

    public AiWordVote() {}

    public AiWordVote(Long userId, Long aidicId, Integer action, LocalDateTime timestamp) {
        this.userId = userId;
        this.aidicId = aidicId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAidicId() { return aidicId; }
    public void setAidicId(Long aidicId) { this.aidicId = aidicId; }

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
