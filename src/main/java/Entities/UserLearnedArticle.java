package Entities;

import java.time.LocalDateTime;

public class UserLearnedArticle {
    private Long userId;
    private Long articleId;
    private LocalDateTime learnedAt;

    public UserLearnedArticle() {}

    public UserLearnedArticle(Long userId, Long articleId, LocalDateTime learnedAt) {
        this.userId = userId;
        this.articleId = articleId;
        this.learnedAt = learnedAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public LocalDateTime getLearnedAt() { return learnedAt; }
    public void setLearnedAt(LocalDateTime learnedAt) { this.learnedAt = learnedAt; }
}
