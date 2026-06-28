package Entities;

public class Article {
    private Long articleId;
    private String title;
    private String content;
    private String source;
    private String difficulty;
    private Integer articleLikeCount;
    private String explanation;
    private Integer explanationLikeCount;
    private Integer explanationDislikeCount;
    private Integer vocquizNum;
    private Integer comquizNum;

    public Article() {}

    public Article(Long articleId, String title, String content, String source,
                   String difficulty, Integer articleLikeCount, String explanation,
                   Integer explanationLikeCount, Integer explanationDislikeCount,
                   Integer vocquizNum, Integer comquizNum) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.source = source;
        this.difficulty = difficulty;
        this.articleLikeCount = articleLikeCount;
        this.explanation = explanation;
        this.explanationLikeCount = explanationLikeCount;
        this.explanationDislikeCount = explanationDislikeCount;
        this.vocquizNum = vocquizNum;
        this.comquizNum = comquizNum;
    }

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getArticleLikeCount() { return articleLikeCount; }
    public void setArticleLikeCount(Integer articleLikeCount) { this.articleLikeCount = articleLikeCount; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Integer getExplanationLikeCount() { return explanationLikeCount; }
    public void setExplanationLikeCount(Integer explanationLikeCount) { this.explanationLikeCount = explanationLikeCount; }

    public Integer getExplanationDislikeCount() { return explanationDislikeCount; }
    public void setExplanationDislikeCount(Integer explanationDislikeCount) { this.explanationDislikeCount = explanationDislikeCount; }

    public Integer getVocquizNum() { return vocquizNum; }
    public void setVocquizNum(Integer vocquizNum) { this.vocquizNum = vocquizNum; }

    public Integer getComquizNum() { return comquizNum; }
    public void setComquizNum(Integer comquizNum) { this.comquizNum = comquizNum; }
}
