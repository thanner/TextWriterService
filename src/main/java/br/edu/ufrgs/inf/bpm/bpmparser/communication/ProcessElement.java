package br.edu.ufrgs.inf.bpm.bpmparser.communication;

import java.util.HashMap;

public class ProcessElement {

    private String className;
    private String individualName;
    private HashMap<String, String> owlDataProperties;
    private HashMap<String, String> owlObjectProperties;

    public ProcessElement(ElementType elementType) {
        this.className = elementType.toString();
        this.individualName = IRIGenerator.getIRIIndividualName(className);
        createHashesMaps();
    }

    public ProcessElement(String className, String individualName) {
        this.className = className;
        this.individualName = individualName;
        createHashesMaps();
    }

    public ProcessElement(String className) {
        this.className = className;
        this.individualName = IRIGenerator.getIRIIndividualName(className);
        createHashesMaps();
    }

    private void createHashesMaps() {
        this.owlDataProperties = new HashMap<>();
        this.owlObjectProperties = new HashMap<>();
    }

    public String getOWLDataProperty(String key) {
        return owlDataProperties.get(key);
    }

    public HashMap<String, String> getOwlDataProperties() {
        return owlDataProperties;
    }

    public void setOwlDataProperties(HashMap<String, String> owlDataProperties) {
        this.owlDataProperties = owlDataProperties;
    }

    public void addDataProperty(String shortForm, String dataProperty) {
        owlDataProperties.put(shortForm, dataProperty);
    }

    public String getOWLObjectProperty(String key) {
        return owlObjectProperties.get(key);
    }

    public HashMap<String, String> getOwlObjectProperties() {
        return owlObjectProperties;
    }

    public void setOwlObjectProperties(HashMap<String, String> owlObjectProperties) {
        this.owlObjectProperties = owlObjectProperties;
    }

    public void addObjectProperty(String shortForm, String objectProperty) {
        owlObjectProperties.put(shortForm, objectProperty);
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ElementType getElementType() {
        return ElementType.valueOf(className.toUpperCase());
    }

    public void setElementType(ElementType elementType) {
        this.className = elementType.toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Individual: ").append(individualName).append("\n");
        stringBuilder.append("Individual Class: ").append(className).append("\n");

        stringBuilder.append("Data Properties:\n");
        for (String key : owlDataProperties.keySet()) {
            stringBuilder.append("Chave: ").append(key).append(" | Valor: ").append(owlDataProperties.get(key)).append("\n");
        }

        stringBuilder.append("Object Properties:\n");
        for (String key : owlObjectProperties.keySet()) {
            stringBuilder.append("Chave: ").append(key).append(" | Valor: ").append(owlObjectProperties.get(key)).append("\n");
        }

        return stringBuilder.append("\n").toString();
    }

}