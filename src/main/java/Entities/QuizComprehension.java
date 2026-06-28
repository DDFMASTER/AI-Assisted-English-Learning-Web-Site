package Entities;

import java.time.LocalDateTime;

public class QuizComprehension {
    private Long comquizId;
    private Long articleId;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isRemoved;
    private LocalDateTime createdAt;

    public QuizComprehension() {}

    public QuizComprehension(Long comquizId, Long articleId, String content,
                             Integer likeCount, Integer dislikeCount,
                             Boolean isRemoved, LocalDateTime createdAt) {
        this.comquizId = comquizId;
        this.articleId = articleId;
        this.content = content;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.isRemoved = isRemoved;
        this.createdAt = createdAt;
    }

    public Long getComquizId() { return comquizId; }
    public void setComquizId(Long comquizId) { this.comquizId = comquizId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }

    public Boolean getIsRemoved() { return isRemoved; }
    public void setIsRemoved(Boolean isRemoved) { this.isRemoved = isRemoved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
