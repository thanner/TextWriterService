package br.edu.ufrgs.inf.bpm.rest.processToText.model;

import java.util.ArrayList;
import java.util.List;

public class Sentence {
    private String value;
    private int level;
    private boolean isLateral;
    private List<Subsentence> subsentenceList;

    public Sentence() {
        subsentenceList = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isLateral() {
        return isLateral;
    }

    public void setLateral(boolean lateral) {
        isLateral = lateral;
    }

    public List<Subsentence> getSubsentenceList() {
        return subsentenceList;
    }

    public void setSubsentenceList(List<Subsentence> subsentenceList) {
        this.subsentenceList = subsentenceList;
    }

}
