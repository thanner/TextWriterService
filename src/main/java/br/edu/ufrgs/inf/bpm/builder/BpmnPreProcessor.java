package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BpmnPreProcessor {

    private TDefinitions tDefinitions;
    private BpmnWrapper processModelWrapper;

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
            tCollaboration.setId(generateId("Collaboration"));

            JAXBElement<TCollaboration> jaxbElement = new ObjectFactory().createCollaboration(tCollaboration);
            tDefinitions.getRootElement().add(jaxbElement);
        } else {
            tCollaboration = tCollaborationList.get(0);
        }

        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            TParticipant tParticipant = processModelWrapper.getParticipantFromProcess(tProcess);
            if (tParticipant != null) {
                // Participant doesnt has a name
                if (tParticipant.getName() == null || tParticipant.getName().isEmpty()) {
                    tParticipant.setName(generateName("Participant"));
                }
            } else {
                // Participant doesnt exists
                tParticipant = new TParticipant();
                tParticipant.setId(generateId("Participant"));
                tParticipant.setName(generateName("Participant"));
                tParticipant.setProcessRef(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", tProcess.getId(), ""));

                tCollaboration.getParticipant().add(tParticipant);
            }
        }
    }

    private void adjustLanes() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            TLaneSet tLaneSet = getTLaneSet(tProcess);
            TLane tCandidateLane = null;

            for (JAXBElement<? extends TFlowElement> jaxbElement : tProcess.getFlowElement()) {
                TFlowElement tFlowElement = jaxbElement.getValue();
                if (!(tFlowElement instanceof TSequenceFlow)) {
                    TLane tLane = processModelWrapper.getLaneByFlowElement(tFlowElement);
                    if (tLane == null) {

                        if (tCandidateLane == null) {
                            tCandidateLane = new TLane();
                            tCandidateLane.setId(generateId("Lane"));
                            tCandidateLane.setName(generateName("Resource"));
                            tLaneSet.getLane().add(tCandidateLane);
                        }

                        tCandidateLane.getFlowNodeRef().add(new ObjectFactory().createTLaneFlowNodeRef(tFlowElement));
                    } else if (tLane.getName() == null || tLane.getName().equals("")) {
                        tLane.setName(generateName("Resource"));
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
            tLaneSet.setId(generateId("LaneSet"));
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
                tActivity.setName("Do unlabeled activity (id: " + tActivity.getId() + ")\n");
            }
        }
    }

    private void adjustStartEvents() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            adjustFlowNodeswithoutIncoming(tProcess);
            adjustNumberStartEvents(tProcess);
        }
    }

    private void adjustEndEvents() {
        for (TProcess tProcess : processModelWrapper.getProcessList()) {
            adjustFlowNodeswithoutOutgoing(tProcess);
            adjustNumberEndEvents(tProcess);
        }
    }

    private void adjustFlowNodeswithoutIncoming(TProcess tProcess) {
        List<TFlowNode> tFlowNodeWithoutIncoming = processModelWrapper.getFlowNodesWithoutIncoming(tProcess);
        for (TFlowNode tFlowNode : tFlowNodeWithoutIncoming) {
            if (!(tFlowNode instanceof TStartEvent)) {
                TStartEvent tStartEvent = createStartEvent(tProcess, processModelWrapper.getLaneByFlowElement(tFlowNode));
                createSequenceFlow(tStartEvent, tFlowNode, tProcess);
            }
        }
    }

    private void adjustFlowNodeswithoutOutgoing(TProcess tProcess) {
        List<TFlowNode> tFlowNodeWithoutOutgoing = processModelWrapper.getFlowNodesWithoutOutgoing(tProcess);
        for (TFlowNode tFlowNode : tFlowNodeWithoutOutgoing) {
            if (!(tFlowNode instanceof TEndEvent)) {
                TEndEvent tEndEvent = createEndEvent(tProcess, processModelWrapper.getLaneByFlowElement(tFlowNode));
                createSequenceFlow(tFlowNode, tEndEvent, tProcess);
            }
        }
    }

    private void adjustNumberStartEvents(TProcess tProcess) {
        List<TStartEvent> tStartEventListOld = processModelWrapper.getFlowElementList(TStartEvent.class, tProcess);
        if (tStartEventListOld.size() > 1) {
            List<TLane> tLaneList = processModelWrapper.getLanesByProcess(tProcess);
            if (tLaneList.size() > 0) {
                TLane tLane = tLaneList.get(0);

                TStartEvent tStartEvent = createStartEvent(tProcess, tLane);
                TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, tLane);
                createSequenceFlow(tStartEvent, tExclusiveGateway, tProcess);

                List<TFlowElement> tFlowElementToRemoveList = new ArrayList<>();
                for (TStartEvent tStartEventOld : tStartEventListOld) {
                    for (QName qNameStartEventOld : tStartEventOld.getOutgoing()) {
                        String idOutgoing = qNameStartEventOld.getLocalPart();
                        TSequenceFlow tSequenceFlowOld = processModelWrapper.getFlowElementById(TSequenceFlow.class, idOutgoing);
                        Object target = tSequenceFlowOld.getTargetRef();
                        if (target instanceof TFlowNode) {
                            createSequenceFlow(tExclusiveGateway, (TFlowNode) target, tProcess);
                        }
                        tFlowElementToRemoveList.add(tSequenceFlowOld);
                    }
                    tFlowElementToRemoveList.add(tStartEventOld);
                }
                removeElements(tFlowElementToRemoveList);
            }
        }
    }

    private void adjustNumberEndEvents(TProcess tProcess) {
        List<TEndEvent> tEndEventListOld = processModelWrapper.getFlowElementList(TEndEvent.class, tProcess);
        if (tEndEventListOld.size() > 1) {
            List<TLane> tLaneList = processModelWrapper.getLanesByProcess(tProcess);
            if (tLaneList.size() > 0) {
                TLane tLane = tLaneList.get(0);

                TEndEvent tEndEvent = createEndEvent(tProcess, tLane);
                TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, tLane);
                createSequenceFlow(tExclusiveGateway, tEndEvent, tProcess);

                List<TFlowElement> tFlowElementToRemoveList = new ArrayList<>();
                for (TEndEvent tEndEventOld : tEndEventListOld) {
                    for (QName qNameEndEventOld : tEndEventOld.getIncoming()) {
                        String idIncoming = qNameEndEventOld.getLocalPart();
                        TSequenceFlow tSequenceFlowOld = processModelWrapper.getFlowElementById(TSequenceFlow.class, idIncoming);
                        Object source = tSequenceFlowOld.getSourceRef();
                        if (source instanceof TFlowNode) {
                            createSequenceFlow((TFlowNode) source, tExclusiveGateway, tProcess);
                        }
                        tFlowElementToRemoveList.add(tSequenceFlowOld);
                    }
                    tFlowElementToRemoveList.add(tEndEventOld);
                }

                removeElements(tFlowElementToRemoveList);
            }
        }
    }

    private void removeElements(List<TFlowElement> flowElementList) {
        Iterator<TFlowElement> flowElementIterator = flowElementList.iterator();
        while (flowElementIterator.hasNext()) {
            TFlowElement flowElementToRemove = flowElementIterator.next();
            if (flowElementToRemove instanceof TSequenceFlow) {
                processModelWrapper.deleteSequenceFlow((TSequenceFlow) flowElementToRemove);
            } else {
                processModelWrapper.deleteFlowElement(flowElementToRemove);
            }
        }
    }

    private TStartEvent createStartEvent(TProcess tProcess, TLane tLane) {
        TStartEvent tStartEvent = new TStartEvent();
        tStartEvent.setId(generateId("StartEvent"));
        positionFlowNode(tProcess, tLane, tStartEvent, new ObjectFactory().createStartEvent(tStartEvent));

        return tStartEvent;
    }

    private TEndEvent createEndEvent(TProcess tProcess, TLane tLane) {
        TEndEvent tEndEvent = new TEndEvent();
        tEndEvent.setId(generateId("EndEvent"));
        positionFlowNode(tProcess, tLane, tEndEvent, new ObjectFactory().createEndEvent(tEndEvent));

        return tEndEvent;
    }

    private TExclusiveGateway createExclusiveGateway(TProcess tProcess, TLane tLane) {
        TExclusiveGateway tExclusiveGateway = new TExclusiveGateway();
        tExclusiveGateway.setId(generateId("ExclusiveGateway"));
        positionFlowNode(tProcess, tLane, tExclusiveGateway, new ObjectFactory().createExclusiveGateway(tExclusiveGateway));

        return tExclusiveGateway;
    }

    private void createSequenceFlow(TFlowNode sourceNode, TFlowNode targetNode, TProcess tProcess) {
        TSequenceFlow tSequenceFlow = new TSequenceFlow();
        tSequenceFlow.setId(generateId("SequenceFlow"));

        tProcess.getFlowElement().add(new ObjectFactory().createSequenceFlow(tSequenceFlow));

        if (sourceNode != null) {
            tSequenceFlow.setSourceRef(sourceNode);
            sourceNode.getOutgoing().add(JaxbWrapper.getQName(tSequenceFlow.getClass(), tSequenceFlow));
        }

        if (targetNode != null) {
            tSequenceFlow.setTargetRef(targetNode);
            targetNode.getIncoming().add(JaxbWrapper.getQName(tSequenceFlow.getClass(), tSequenceFlow));
        }
    }

    private void positionFlowNode(TProcess tProcess, TLane tLane, TFlowNode tFlowNode, Object jaxbFlowNode) {
        tProcess.getFlowElement().add((JAXBElement<? extends TFlowElement>) jaxbFlowNode);
        tLane.getFlowNodeRef().add(new ObjectFactory().createTLaneFlowNodeRef(tFlowNode));
    }

    private String generateId(String idName) {
        int idValue = 1;
        List<String> idList = processModelWrapper.getElementsId();

        String newId;
        do {
            newId = idName + " " + idValue;
            idValue++;
        } while (idList.contains(newId));
        return newId;
    }

    private String generateName(String name) {
        int idValue = 1;
        List<String> idList = processModelWrapper.getElementsName();

        String newName;
        do {
            newName = name + " " + idValue;
            idValue++;
        } while (idList.contains(newName));
        return newName;
    }

}
