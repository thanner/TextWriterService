package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;

public class BpmnPreProcessor {

    private TDefinitions tDefinitions;
    private BpmnWrapper processModelWrapper;

    private int laneSetId = 1;
    private int startEventId = 1;

    public BpmnPreProcessor(TDefinitions tDefinitions) {
        this.tDefinitions = tDefinitions;
        processModelWrapper = new BpmnWrapper(tDefinitions);
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }

    public void preProcessing() throws IllegalArgumentException {
        verifySequenceFlowsWithoutElements();
        adjustPools();
        adjustLanes();
        adjustStartEvents();
        adjustEndEvents();
        adjustActivityLabel();
    }

    private void verifySequenceFlowsWithoutElements() throws IllegalArgumentException {
        List<TSequenceFlow> sequenceFlowList = processModelWrapper.getFlowElementList(TSequenceFlow.class);
        for (TSequenceFlow tSequenceFlow : sequenceFlowList) {
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
            if (tParticipant != null) {
                // Participant doesnt has a name
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
        List<TActivity> tActivityList = processModelWrapper.getFlowElementList(TActivity.class);
        for (TActivity tActivity : tActivityList) {
            String name = tActivity.getName();
            if (name == null || name.replaceAll("\n", "").isEmpty()) {
                tActivity.setName("Do activity with id " + tActivity.getId() + "\n");
            }
        }
    }

    private void adjustStartEvents() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            adjustFlowNodeswithoutIncoming(tProcess);
            adjustNumberStartEvents(tProcess);
        }
    }

    private void adjustFlowNodeswithoutIncoming(TProcess tProcess) {
        List<TFlowNode> tFlowNodeWithoutIncoming = processModelWrapper.getFlowNodesWithoutIncoming(tProcess);
        for (TFlowNode tFlowNode : tFlowNodeWithoutIncoming) {
            TStartEvent tStartEvent = createStartEvent(tProcess, processModelWrapper.getLaneByFlowElement(tFlowNode));
            createSequenceFlow(tStartEvent, tFlowNode, tProcess);
        }
    }

    private void adjustNumberStartEvents(TProcess tProcess) {
        List<TStartEvent> tStartEventListOld = processModelWrapper.getFlowElementList(TStartEvent.class, tProcess);
        if (tStartEventListOld.size() > 1) {
            List<TLane> tLaneList = processModelWrapper.getLanesByProcess(tProcess);
            if (tLaneList.size() > 0) {
                TLane tLane = tLaneList.get(0);

                // Cria novo start event
                TStartEvent tStartEvent = createStartEvent(tProcess, tLane);

                // Criar arcos do novo start event pros elementos dos antigos start events
                for (TStartEvent tStartEventOld : tStartEventListOld) {
                    for (QName qName : tStartEventOld.getOutgoing()) {
                        String idOutgoing = qName.getLocalPart();
                        TSequenceFlow tSequenceFlow = processModelWrapper.getFlowElementById(TSequenceFlow.class, idOutgoing);
                        Object target = tSequenceFlow.getTargetRef();
                        if (target instanceof TFlowNode) {
                            createSequenceFlow(tStartEvent, (TFlowNode) target, tProcess);
                        }
                    }
                }
            }

            // Retirar todos os antigos start events e seus respectivos sequence flows!!!!
            // TODO: fazer
        }
    }

    private void adjustEndEvents() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            adjustFlowNodeswithoutOutgoing(tProcess);
            adjustNumberEndEvents(tProcess);
        }
    }

    // TODO: FaZer
    private void adjustFlowNodeswithoutOutgoing(TProcess tProcess) {

    }

    // TODO: Fazer
    private void adjustNumberEndEvents(TProcess tProcess) {

    }

    private TStartEvent createStartEvent(TProcess tProcess, TLane tLane) {
        TStartEvent tStartEvent = new TStartEvent();
        tStartEvent.setId("StartEvent " + startEventId++);
        positionFlowNode(tProcess, tLane, tStartEvent, new ObjectFactory().createStartEvent(tStartEvent));

        return tStartEvent;
    }

    private void createSequenceFlow(TFlowNode source, TFlowNode target, TProcess tProcess) {
        TSequenceFlow tSequenceFlow = new TSequenceFlow();
        tSequenceFlow.setSourceRef(source);
        tSequenceFlow.setTargetRef(target);
        tProcess.getFlowElement().add(new ObjectFactory().createSequenceFlow(tSequenceFlow));
    }

    private void positionFlowNode(TProcess tProcess, TLane tLane, TFlowNode tFlowNode, Object jaxbFlowNode) {
        tProcess.getFlowElement().add((JAXBElement<? extends TFlowElement>) jaxbFlowNode);
        tLane.getFlowNodeRef().add(new ObjectFactory().createTLaneFlowNodeRef(tFlowNode));
    }

}
