package Entities;

import java.time.LocalDateTime;

public class AiWordDic {
    private Long aidicId;
    private String word;
    private String phonetic;
    private String translation;
    private String explanation;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isRemoved;
    private LocalDateTime createdAt;

    public AiWordDic() {}

    public AiWordDic(Long aidicId, String word, String phonetic, String translation,
                     String explanation, Integer likeCount, Integer dislikeCount,
                     Boolean isRemoved, LocalDateTime createdAt) {
        this.aidicId = aidicId;
        this.word = word;
        this.phonetic = phonetic;
        this.translation = translation;
        this.explanation = explanation;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.isRemoved = isRemoved;
        this.createdAt = createdAt;
    }

    public Long getAidicId() { return aidicId; }
    public void setAidicId(Long aidicId) { this.aidicId = aidicId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }

    public Boolean getIsRemoved() { return isRemoved; }
    public void setIsRemoved(Boolean isRemoved) { this.isRemoved = isRemoved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
