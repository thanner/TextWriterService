package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;
import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.util.XmlFormat;
import org.w3c.dom.Document;

import java.util.Comparator;

public class ProcessElementDocument {

    public static final Comparator<ProcessElementDocument> PER_LENGTH = (a1, a2) -> {
        SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
        Integer sizeA1 = surfaceRealizer.getSentenceForIndex(a1.getSentence()).length();
        Integer sizeA2 = surfaceRealizer.getSentenceForIndex(a2.getSentence()).length();

        return sizeA2.compareTo(sizeA1);
    };

    private String processElementId;
    private ProcessElementType processElement;
    private String resourceName;
    private Document document;
    private String sentence;

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

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getSentence() {
        if (sentence != null) {
            return sentence;
        } else {
            SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
            return cleanSubsentence(surfaceRealizer.realizeSentence(document));
        }
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    private String cleanSubsentence(String subsentence) {
        if (subsentence != null && !subsentence.isEmpty()) {
            return subsentence.substring(0, subsentence.length() - 1);
        }
        return "";
    }

    public void fixDocument() {
        if (document != null) {
            document = XmlFormat.getClone(document);
        }
    }

}
