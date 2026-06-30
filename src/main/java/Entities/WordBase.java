package Entities;

public class WordBase {
    private Long id;
    private String word;
    private String phonetic;
    private String translation;
    private String explanation;

    public WordBase() {}

    public WordBase(Long id, String word, String phonetic, String translation) {
        this.id = id;
        this.word = word;
        this.phonetic = phonetic;
        this.translation = translation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
