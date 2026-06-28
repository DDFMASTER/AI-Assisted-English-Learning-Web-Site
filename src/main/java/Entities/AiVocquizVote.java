package Entities;

import java.time.LocalDateTime;

public class AiVocquizVote {
    private Long userId;
    private Long vocquizId;
    private Integer action;
    private LocalDateTime timestamp;

    public AiVocquizVote() {}

    public AiVocquizVote(Long userId, Long vocquizId, Integer action, LocalDateTime timestamp) {
        this.userId = userId;
        this.vocquizId = vocquizId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVocquizId() { return vocquizId; }
    public void setVocquizId(Long vocquizId) { this.vocquizId = vocquizId; }

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
