package br.edu.ufrgs.inf.bpm.bpmparser.communication;

public class IRIGenerator {
    private static String baseName = "Ind";
    private static int idIRI = 0;

    public static String getIRIIndividualName(String name) {
        idIRI++;
        return baseName + idIRI + "-" + name;
    }
}
