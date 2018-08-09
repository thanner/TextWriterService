package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.*;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;

public class BpmnPreProcessor {

    private TDefinitions tDefinitions;
    private BpmnWrapper processModelWrapper;

    private int laneSetId = 1;


    public BpmnPreProcessor(TDefinitions tDefinitions) {
        this.tDefinitions = tDefinitions;
        processModelWrapper = new BpmnWrapper(tDefinitions);
    }

    public void preProcessing() throws IllegalArgumentException {
        verifySequenceFlowsWithoutElements();
        adjustPools();
        adjustLanes();
        adjustActivityLabel();
    }

    private void verifySequenceFlowsWithoutElements() throws IllegalArgumentException {
        for (TSequenceFlow tSequenceFlow : processModelWrapper.getSequenceFlowList()) {
            if (tSequenceFlow.getSourceRef() == null) {
                throw new IllegalArgumentException("In Sequence Flow with id: " + tSequenceFlow.getId() + " the source is null");
            }
            if (tSequenceFlow.getTargetRef() == null) {
                throw new IllegalArgumentException("In Sequence Flow with id: " + tSequenceFlow.getId() + " the target is null");
            }
        }
    }

    private void adjustPools() {
        TCollaboration tCollaboration;

        List<TCollaboration> tCollaborationList = processModelWrapper.getCollaborationList();
        if (tCollaborationList.isEmpty()) {
            tCollaboration = new TCollaboration();
            tCollaboration.setId("CollaborationId");

            JAXBElement<TCollaboration> jaxbElement = new ObjectFactory().createCollaboration(tCollaboration);
            tDefinitions.getRootElement().add(jaxbElement);
        } else {
            tCollaboration = tCollaborationList.get(0);
        }

        int processId = 1;
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            TParticipant tParticipant = processModelWrapper.getParticipantFromProcess(tProcess);
            // Participant doesnt has a name
            if (tParticipant != null) {
                if (tParticipant.getName() == null || tParticipant.getName().isEmpty()) {
                    tParticipant.setName("Participant " + processId++);
                }
            } else {
                // Participant doesnt exists
                tParticipant = new TParticipant();
                tParticipant.setId("ParticipantId " + processId);
                tParticipant.setName("Participant " + processId);
                tParticipant.setProcessRef(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", tProcess.getId(), ""));

                tCollaboration.getParticipant().add(tParticipant);

                processId++;
            }
        }
    }

    private void adjustLanes() {
        int laneId = 1;

        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            TLaneSet tLaneSet = getTLaneSet(tProcess);
            TLane tCandidateLane = null;

            for (JAXBElement<? extends TFlowElement> jaxbElement : tProcess.getFlowElement()) {
                TFlowElement tFlowElement = jaxbElement.getValue();
                if (!(tFlowElement instanceof TSequenceFlow)) {
                    if (processModelWrapper.getLaneByFlowElement(tFlowElement) == null) {

                        if (tCandidateLane == null) {
                            tCandidateLane = new TLane();
                            tCandidateLane.setId("LaneId " + laneId);
                            tCandidateLane.setName("Resource " + laneId);
                            tLaneSet.getLane().add(tCandidateLane);
                            laneId++;
                        }

                        tCandidateLane.getFlowNodeRef().add(new ObjectFactory().createTLaneFlowNodeRef(tFlowElement));
                    }
                }
            }
        }
    }

    private TLaneSet getTLaneSet(TProcess tProcess) {
        TLaneSet tLaneSet;

        List<TLaneSet> tLaneSetList = tProcess.getLaneSet();
        if (tLaneSetList.isEmpty()) {
            tLaneSet = new TLaneSet();
            tLaneSet.setId("LaneSet " + laneSetId++);
            tProcess.getLaneSet().add(tLaneSet);
        } else {
            tLaneSet = tLaneSetList.get(0);
        }

        return tLaneSet;
    }

    private void adjustActivityLabel() {
        for (TActivity tActivity : processModelWrapper.getActivityList()) {
            String name = tActivity.getName();
            if (name == null || name.replaceAll("\n", "").isEmpty()) {
                tActivity.setName("Do activity with id " + tActivity.getId() + "\n");
            }
        }
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }
}
