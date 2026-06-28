package Entities;

import java.time.LocalDateTime;

public class AiComquizVote {
    private Long userId;
    private Long comquizId;
    private Integer action;
    private LocalDateTime timestamp;

    public AiComquizVote() {}

    public AiComquizVote(Long userId, Long comquizId, Integer action, LocalDateTime timestamp) {
        this.userId = userId;
        this.comquizId = comquizId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getComquizId() { return comquizId; }
    public void setComquizId(Long comquizId) { this.comquizId = comquizId; }

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
