package br.edu.ufrgs.inf.bpm.rest.textwriter.model;

public class Subsentence {

    private String processElementId;
    private String processElement;
    private String resource;
    private int startIndex;
    private int endIndex;

    public String getProcessElementId() {
        return processElementId;
    }

    public void setProcessElementId(String processElementId) {
        this.processElementId = processElementId;
    }

    public String getProcessElement() {
        return processElement;
    }

    public void setProcessElement(String processElement) {
        this.processElement = processElement;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

}
