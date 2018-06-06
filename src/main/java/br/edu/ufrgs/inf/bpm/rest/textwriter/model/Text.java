package br.edu.ufrgs.inf.bpm.rest.textwriter.model;

import java.util.ArrayList;
import java.util.List;

public class Text {
    private List<Sentence> sentenceList;

    public Text() {
        sentenceList = new ArrayList<>();
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public void setSentenceList(List<Sentence> sentenceList) {
        this.sentenceList = sentenceList;
    }

}
