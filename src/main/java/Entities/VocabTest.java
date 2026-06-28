package Entities;

public class VocabTest {
    private Integer vocabId;
    private String word;
    private Boolean isPseudo;

    public VocabTest() {}

    public VocabTest(Integer vocabId, String word, Boolean isPseudo) {
        this.vocabId = vocabId;
        this.word = word;
        this.isPseudo = isPseudo;
    }

    public Integer getVocabId() { return vocabId; }
    public void setVocabId(Integer vocabId) { this.vocabId = vocabId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public Boolean getIsPseudo() { return isPseudo; }
    public void setIsPseudo(Boolean isPseudo) { this.isPseudo = isPseudo; }
}
