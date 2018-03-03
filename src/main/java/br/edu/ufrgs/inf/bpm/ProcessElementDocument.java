package br.edu.ufrgs.inf.bpm;

import org.w3c.dom.Document;

public class ProcessElementDocument {

    private String resource;
    private String processElement;
    private Document document;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getProcessElement() {
        return processElement;
    }

    public void setProcessElement(String processElement) {
        this.processElement = processElement;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
