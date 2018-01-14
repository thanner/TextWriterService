package br.edu.ufrgs.inf.bpm.bpmparser.wrapper;

import br.edu.ufrgs.inf.bpm.bpmparser.communication.ElementType;
import br.edu.ufrgs.inf.bpm.bpmparser.communication.ProcessElement;
import br.edu.ufrgs.inf.bpm.bpmparser.ontology.OntologyNames;
import br.edu.ufrgs.inf.bpm.bpmparser.ontology.OntologyWrapper;
import processToText.dataModel.jsonIntermediate.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyOutput {

    private static List<ProcessElement> processElements;

    public static List<JSONPool> getPools() {
        updateElements();
        return processElements.stream().filter(p -> p.getElementType().equals(ElementType.POOL)).map(individual -> new JSONPool(getId(individual), getName(individual))).collect(Collectors.toList());
    }

    public static List<JSONLane> getLanes() {
        return new ArrayList<>();
    }

    public static List<JSONTask> getActivities() {
        return new ArrayList<>();
    }

    public static List<JSONEvent> getEvents() {
        return new ArrayList<>();
    }

    public static List<JSONGateway> getGateways() {
        return new ArrayList<>();
    }

    public static List<JSONElem> getElements() {
        return new ArrayList<>();
    }

    private static void updateElements() {
        if (processElements == null) {
            processElements = OntologyWrapper.getCompleteIndividuals();
        }
    }

    private static int getId(ProcessElement individual) {
        return Integer.valueOf(individual.getOwlDataProperties().get(OntologyNames.ID.toString()).toString());
    }

    private static String getName(ProcessElement individual) {
        return individual.getOwlDataProperties().get(OntologyNames.NAME.toString()).toString();
    }
}
