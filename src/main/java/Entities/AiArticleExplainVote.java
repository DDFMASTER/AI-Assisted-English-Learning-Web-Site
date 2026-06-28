package Entities;

import java.time.LocalDateTime;

public class AiArticleExplainVote {
    private Long userId;
    private Long articleId;
    private Integer action;
    private LocalDateTime timestamp;

    public AiArticleExplainVote() {}

    public AiArticleExplainVote(Long userId, Long articleId, Integer action, LocalDateTime timestamp) {
        this.userId = userId;
        this.articleId = articleId;
        this.action = action;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
