package br.edu.ufrgs.inf.bpm.rest.textwriter.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
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

    public void appendSentences(Text newText) {
        getSentenceList().addAll(newText.getSentenceList());
    }

}
