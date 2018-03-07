package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.util.XmlFormat;
import org.w3c.dom.Document;

public class ProcessElementDocument {

    private String resource;
    private String processElement;
    private Document document;
    private String sentence;

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

    public void setDocument(Document document) {
        this.document = document;
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
        return subsentence.substring(0, subsentence.length() - 1);
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
