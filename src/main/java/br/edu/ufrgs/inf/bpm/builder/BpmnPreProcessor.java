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
                if (tParticipant.getName().isEmpty()) {
                    tParticipant.setName("Participant " + processId++);
                }
            } else {
                // Participant doesnt exists
                tParticipant = new TParticipant();
                tParticipant.setId("Participant " + processId);
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
            List<TLane> tLaneList = tLaneSet.getLane();

            for (JAXBElement<? extends TFlowElement> tFlowElement : tProcess.getFlowElement()) {
                if (processModelWrapper.getLaneByFlowElement(tFlowElement.getValue()) == null) {
                    // Se a tlane desse processo já existe
                    // Usa a tlane
                    // Senão
                    // Cria a tLane (Id: laneId | Name: Resource laneId)

                    // Adiciona o elemento na tLane
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
            if (tActivity.getName().replaceAll("\n", "").isEmpty()) {
                tActivity.setName("Do activity with id " + tActivity.getId() + "\n");
            }
        }
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }
}
