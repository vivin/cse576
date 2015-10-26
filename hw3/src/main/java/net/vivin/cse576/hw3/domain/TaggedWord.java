package net.vivin.cse576.hw3.domain;

public class TaggedWord {

    private PartOfSpeechTag partOfSpeechTag;
    private String word;

    public TaggedWord(PartOfSpeechTag partOfSpeechTag, String word) {
        this.partOfSpeechTag = partOfSpeechTag;
        this.word = word;
    }

    public PartOfSpeechTag getPartOfSpeechTag() {
        return partOfSpeechTag;
    }

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return "[" + word + "/" + partOfSpeechTag + "]";
    }
}
