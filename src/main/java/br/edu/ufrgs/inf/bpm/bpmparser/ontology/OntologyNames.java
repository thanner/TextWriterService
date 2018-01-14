package br.edu.ufrgs.inf.bpm.bpmparser.ontology;

public enum OntologyNames {
    ID("id"),
    NAME("name");

    private final String name;

    OntologyNames(String string) {
        this.name = string;
    }

    public String toString() {
        return this.name;
    }
}
