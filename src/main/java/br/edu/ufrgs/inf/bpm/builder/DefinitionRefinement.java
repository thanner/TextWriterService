package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.*;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;

import javax.xml.bind.JAXBElement;

public class DefinitionRefinement {

    private TDefinitions tDefinitions;
    private BpmnWrapper processModelWrapper;

    public DefinitionRefinement(TDefinitions tDefinitions) {
        this.tDefinitions = tDefinitions;
        processModelWrapper = new BpmnWrapper(tDefinitions);
    }

    public void adjustTDefinitions() throws IllegalArgumentException {
        refineSequenceFlowsWithoutElements();
        refineActivityLabel();
        refinePools();
        refineLanes();
    }

    private void refineSequenceFlowsWithoutElements() throws IllegalArgumentException {
        for (TSequenceFlow tSequenceFlow : processModelWrapper.getSequenceFlowList()) {
            if (tSequenceFlow.getSourceRef() == null) {
                throw new IllegalArgumentException("In Sequence Flow with id: " + tSequenceFlow.getId() + " the source is null");
            }
            if (tSequenceFlow.getTargetRef() == null) {
                throw new IllegalArgumentException("In Sequence Flow with id: " + tSequenceFlow.getId() + " the target is null");
            }
        }
    }

    // TODO: Fazer

    private void refineLanes() {
    }

    private void refinePools() {
    }

    private void refineActivityLabel() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                if (flowElement.getValue() instanceof TActivity) {
                    TActivity tActivity = (TActivity) flowElement.getValue();
                    if (tActivity.getName().replaceAll("\n", "").isEmpty()) {
                        tActivity.setName("Do activity with id " + tActivity.getId() + "\n");
                    }
                }
            }
        }
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }
}
