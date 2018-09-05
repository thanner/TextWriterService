package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.metatext.ProcessElementType;
import br.edu.ufrgs.inf.bpm.util.XmlFormat;
import org.w3c.dom.Document;

public class ProcessElementDocument {

    private String resource;
    private String processElementId;
    private ProcessElementType processElement;
    private Document document;
    private String sentence;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getProcessElementId() {
        return this.processElementId;
    }

    public void setProcessElementId(String processElementId) {
        this.processElementId = processElementId;
    }

    public ProcessElementType getProcessElementType() {
        return processElement;
    }

    public void setProcessElementType(ProcessElementType processElement) {
        this.processElement = processElement;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public String getSentence(){
        if(sentence != null){
            return sentence;
        } else {
            SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
            return cleanSubsentence(surfaceRealizer.realizeSentence(document));
        }
    }

    private String cleanSubsentence(String subsentence){
        if (subsentence != null && !subsentence.isEmpty()) {
            return subsentence.substring(0, subsentence.length() - 1);
        }
        return "";
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void fixDocument() {
        if (document != null) {
            document = XmlFormat.getClone(document);
        }
    }

}
