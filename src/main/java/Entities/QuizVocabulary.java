package Entities;

import java.time.LocalDateTime;

public class QuizVocabulary {
    private Long vocquizId;
    private Long articleId;
    private String word;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isRemoved;
    private LocalDateTime createdAt;

    public QuizVocabulary() {}

    public QuizVocabulary(Long vocquizId, Long articleId, String word, String content,
                          Integer likeCount, Integer dislikeCount,
                          Boolean isRemoved, LocalDateTime createdAt) {
        this.vocquizId = vocquizId;
        this.articleId = articleId;
        this.word = word;
        this.content = content;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.isRemoved = isRemoved;
        this.createdAt = createdAt;
    }

    public Long getVocquizId() { return vocquizId; }
    public void setVocquizId(Long vocquizId) { this.vocquizId = vocquizId; }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

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
