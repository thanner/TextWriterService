package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.TActivity;
import br.edu.ufrgs.inf.bpm.bpmn.TDefinitions;
import br.edu.ufrgs.inf.bpm.bpmn.TSequenceFlow;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;

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
        for (TActivity tActivity : processModelWrapper.getActivityList()) {
            if (tActivity.getName().replaceAll("\n", "").isEmpty()) {
                tActivity.setName("Do activity with id " + tActivity.getId() + "\n");
            }
        }
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }
}
