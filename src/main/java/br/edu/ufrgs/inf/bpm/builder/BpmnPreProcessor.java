package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;

public class BpmnPreProcessor {

    private TDefinitions tDefinitions;
    private BpmnWrapper bpmnWrapper;

    public BpmnPreProcessor(TDefinitions tDefinitions) {
        this.tDefinitions = tDefinitions;
        bpmnWrapper = new BpmnWrapper(tDefinitions);
    }

    public TDefinitions gettDefinitions() {
        return tDefinitions;
    }

    public void preProcessing() throws IllegalArgumentException {
        verifySequenceFlowsWithoutElements();
        adjustPools();
        adjustLanes();
        adjustBoundaryEvents();
        adjustIncomingAndOutgoing();
        adjustStartEvents();
        adjustEndEvents();
        adjustLabel();
        adjustActivityLabel();
    }

    private void verifySequenceFlowsWithoutElements() throws IllegalArgumentException {
        List<TSequenceFlow> sequenceFlowList = bpmnWrapper.getFlowElementList(TSequenceFlow.class);
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

        List<TCollaboration> tCollaborationList = bpmnWrapper.getCollaborationList();
        if (tCollaborationList.isEmpty()) {
            tCollaboration = new TCollaboration();
            tCollaboration.setId(generateId("Collaboration"));

            JAXBElement<TCollaboration> jaxbElement = new ObjectFactory().createCollaboration(tCollaboration);
            tDefinitions.getRootElement().add(jaxbElement);
        } else {
            tCollaboration = tCollaborationList.get(0);
        }

        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            if (tProcess.getName() == null || tProcess.getName().isEmpty()) {
                tProcess.setName(generateName("Process"));
            }
            String processName = tProcess.getName();

            TParticipant tParticipant = bpmnWrapper.getParticipantFromProcess(tProcess);
            if (tParticipant != null) {
                // Participant doesnt has a name
                if (tParticipant.getName() == null || tParticipant.getName().isEmpty()) {
                    tParticipant.setName(processName);
                }
            } else {
                // Participant doesnt exists
                tParticipant = new TParticipant();
                tParticipant.setId(generateId("Participant"));
                tParticipant.setName(generateName(processName));
                tParticipant.setProcessRef(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL", tProcess.getId(), ""));

                tCollaboration.getParticipant().add(tParticipant);
            }
        }
    }

    private void adjustLanes() {
        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            TLaneSet tLaneSet = getTLaneSet(tProcess);
            TLane tCandidateLane = null;

            for (JAXBElement<? extends TFlowElement> jaxbElement : tProcess.getFlowElement()) {
                TFlowElement tFlowElement = jaxbElement.getValue();
                if (!(tFlowElement instanceof TSequenceFlow)) {
                    TLane tLane = bpmnWrapper.getLaneByFlowElement(tFlowElement);
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

    private void adjustBoundaryEvents() {
        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            for (TFlowElement tFlowElement : bpmnWrapper.getFlowElementListDeep(tProcess)) {
                if (tFlowElement instanceof TBoundaryEvent) {
                    TBoundaryEvent tBoundaryEvent = (TBoundaryEvent) tFlowElement;
                    TActivity tActivityWithAttach = bpmnWrapper.getTActivityAttached(tProcess, tBoundaryEvent);
                    createSequenceFlow(tActivityWithAttach, tBoundaryEvent, tProcess);
                }
            }
        }
    }

    private void adjustIncomingAndOutgoing() {
        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            adjustFlowElementWithMultipleIncoming(tProcess);
            adjustFlowElementWithMultipleOutgoing(tProcess);
        }
    }

    private void adjustFlowElementWithMultipleOutgoing(TProcess tProcess) {
        for (TFlowElement tFlowElement : bpmnWrapper.getFlowElementListDeep(tProcess)) {
            if (tFlowElement instanceof TFlowNode) {
                TFlowNode tFlowNode = (TFlowNode) tFlowElement;
                if (!(tFlowElement instanceof TGateway)) {
                    List<TFlowElement> targetElementList = bpmnWrapper.getFlowNodeTargetList(tFlowNode);
                    if (targetElementList.size() > 1) {
                        for (TSequenceFlow tSequenceFlow : bpmnWrapper.getSequenceFlowTargetList(tFlowNode)) {
                            bpmnWrapper.deleteSequenceFlow(tSequenceFlow);
                        }

                        TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, bpmnWrapper.getBaseElement(tFlowNode));
                        createSequenceFlow(tFlowNode, tExclusiveGateway, tProcess);
                        for (TFlowElement targetElement : targetElementList) {
                            if (targetElement instanceof TFlowNode) {
                                TFlowNode targetNode = (TFlowNode) targetElement;
                                createSequenceFlow(tExclusiveGateway, targetNode, tProcess);
                            }
                        }
                    }
                }
            }
        }
    }

    private void adjustFlowElementWithMultipleIncoming(TProcess tProcess) {
        for (TFlowElement tFlowElement : bpmnWrapper.getFlowElementListDeep(tProcess)) {
            if (tFlowElement instanceof TFlowNode) {
                TFlowNode tFlowNode = (TFlowNode) tFlowElement;
                if (!(tFlowElement instanceof TGateway)) {
                    List<TFlowElement> sourceElementList = bpmnWrapper.getFlowNodeSourceList(tFlowNode);
                    if (sourceElementList.size() > 1) {
                        for (TSequenceFlow tSequenceFlow : bpmnWrapper.getSequenceFlowSourceList(tFlowNode)) {
                            bpmnWrapper.deleteSequenceFlow(tSequenceFlow);
                        }

                        TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, bpmnWrapper.getBaseElement(tFlowNode));
                        createSequenceFlow(tExclusiveGateway, tFlowNode, tProcess);
                        for (TFlowElement sourceElement : sourceElementList) {
                            if (sourceElement instanceof TFlowNode) {
                                TFlowNode sourceNode = (TFlowNode) sourceElement;
                                createSequenceFlow(sourceNode, tExclusiveGateway, tProcess);
                            }
                        }
                    }
                }
            }
        }
    }

    private void adjustStartEvents() {
        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            //adjustStartEventsWithMultipleOutgoing(tProcess);
            adjustFlowNodeswithoutIncoming(tProcess);
            adjustNumberStartEvents(tProcess);
        }
    }

    private void adjustEndEvents() {
        for (TProcess tProcess : bpmnWrapper.getProcessList()) {
            //adjustEndEventsWithMultipleIncoming(tProcess);
            adjustFlowNodeswithoutOutgoing(tProcess);
            adjustNumberEndEvents(tProcess);
        }
    }

    /*
    private void adjustStartEventsWithMultipleOutgoing(TProcess tProcess) {
        for (TStartEvent tStartEvent : bpmnWrapper.getFlowElementListDeep(TStartEvent.class, tProcess)) {
            List<TFlowElement> targetElementList = bpmnWrapper.getFlowNodeTargetList(tStartEvent);
            if (targetElementList.size() > 1) {
                for (TSequenceFlow tSequenceFlow : bpmnWrapper.getSequenceFlowTargetList(tStartEvent)) {
                    bpmnWrapper.deleteSequenceFlow(tSequenceFlow);
                }

                TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, bpmnWrapper.getBaseElement(tStartEvent));
                createSequenceFlow(tStartEvent, tExclusiveGateway, tProcess);
                for (TFlowElement targetElement : targetElementList) {
                    if (targetElement instanceof TFlowNode) {
                        TFlowNode targetNode = (TFlowNode) targetElement;
                        createSequenceFlow(tExclusiveGateway, targetNode, tProcess);
                    }
                }
            }
        }
    }

    private void adjustEndEventsWithMultipleIncoming(TProcess tProcess) {
        for (TEndEvent tEndEvent : bpmnWrapper.getFlowElementListDeep(TEndEvent.class, tProcess)) {
            List<TFlowElement> sourceElementList = bpmnWrapper.getFlowNodeSourceList(tEndEvent);
            if (sourceElementList.size() > 1) {
                for (TSequenceFlow tSequenceFlow : bpmnWrapper.getSequenceFlowSourceList(tEndEvent)) {
                    bpmnWrapper.deleteSequenceFlow(tSequenceFlow);
                }

                TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, bpmnWrapper.getBaseElement(tEndEvent));
                createSequenceFlow(tExclusiveGateway, tEndEvent, tProcess);
                for (TFlowElement sourceElement : sourceElementList) {
                    if (sourceElement instanceof TFlowNode) {
                        TFlowNode sourceNode = (TFlowNode) sourceElement;
                        createSequenceFlow(sourceNode, tExclusiveGateway, tProcess);
                    }
                }
            }
        }
    }
    */

    private void adjustFlowNodeswithoutIncoming(TProcess tProcess) {
        List<TFlowNode> tFlowNodeWithoutIncoming = bpmnWrapper.getFlowNodesWithoutIncomingDeep(tProcess);
        for (TFlowNode tFlowNode : tFlowNodeWithoutIncoming) {
            if (!(tFlowNode instanceof TStartEvent) && !(tFlowNode instanceof TBoundaryEvent)) {
                TStartEvent tStartEvent = createStartEvent(tProcess, bpmnWrapper.getBaseElement(tFlowNode));
                createSequenceFlow(tStartEvent, tFlowNode, tProcess);
            }
        }
    }

    private void adjustFlowNodeswithoutOutgoing(TProcess tProcess) {
        List<TFlowNode> tFlowNodeWithoutOutgoing = bpmnWrapper.getFlowNodesWithoutOutgoingDeep(tProcess);
        for (TFlowNode tFlowNode : tFlowNodeWithoutOutgoing) {
            if (!(tFlowNode instanceof TEndEvent)) {
                TEndEvent tEndEvent = createEndEvent(tProcess, bpmnWrapper.getBaseElement(tFlowNode));
                createSequenceFlow(tFlowNode, tEndEvent, tProcess);
            }
        }
    }


    private void adjustNumberStartEvents(TProcess tProcess) {
        List<TStartEvent> tStartEventProcessList = bpmnWrapper.getFlowElementListDeep(TStartEvent.class, tProcess);

        Map<TSubProcess, List<TStartEvent>> startEventMap = new HashMap<>();
        for (TStartEvent tStartEvent : tStartEventProcessList) {
            TSubProcess tSubProcess = bpmnWrapper.getSubprocessByFlowElement(tStartEvent);
            if (!startEventMap.containsKey(tSubProcess)) {
                startEventMap.put(tSubProcess, new ArrayList<>());
            }
            startEventMap.get(tSubProcess).add(tStartEvent);
        }

        for (Map.Entry<TSubProcess, List<TStartEvent>> startEventGroup : startEventMap.entrySet()) {
            List<TStartEvent> tStartEventListOld = startEventGroup.getValue();
            if (tStartEventListOld.size() > 1) {
                TBaseElement tBaseElement = null;
                if (startEventGroup.getKey() == null) {
                    List<TLane> tLaneList = bpmnWrapper.getLanesByProcess(tProcess);
                    if (tLaneList.size() > 0) {
                        tBaseElement = tLaneList.get(0);
                    }
                } else {
                    tBaseElement = startEventGroup.getKey();
                }
                if (tBaseElement != null) {
                    TStartEvent tStartEvent = createStartEvent(tProcess, tBaseElement);
                    TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, tBaseElement);
                    createSequenceFlow(tStartEvent, tExclusiveGateway, tProcess);

                    List<TFlowElement> tFlowElementToRemoveList = new ArrayList<>();
                    for (TStartEvent tStartEventOld : tStartEventListOld) {
                        for (QName qNameStartEventOld : tStartEventOld.getOutgoing()) {
                            String idOutgoing = qNameStartEventOld.getLocalPart();
                            TSequenceFlow tSequenceFlowOld = (TSequenceFlow) bpmnWrapper.getFlowElementById(idOutgoing);
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
    }

    private void adjustNumberEndEvents(TProcess tProcess) {
        List<TEndEvent> tEndEventProcessList = bpmnWrapper.getFlowElementListDeep(TEndEvent.class, tProcess);

        Map<TSubProcess, List<TEndEvent>> endEventMap = new HashMap<>();
        for (TEndEvent tEndEvent : tEndEventProcessList) {
            TSubProcess tSubProcess = bpmnWrapper.getSubprocessByFlowElement(tEndEvent);
            if (!endEventMap.containsKey(tSubProcess)) {
                endEventMap.put(tSubProcess, new ArrayList<>());
            }
            endEventMap.get(tSubProcess).add(tEndEvent);
        }

        for (Map.Entry<TSubProcess, List<TEndEvent>> endEventGroup : endEventMap.entrySet()) {
            List<TEndEvent> tEndEventListOld = endEventGroup.getValue();
            if (tEndEventListOld.size() > 1) {
                TBaseElement tBaseElement = null;
                if (endEventGroup.getKey() == null) {
                    List<TLane> tLaneList = bpmnWrapper.getLanesByProcess(tProcess);
                    if (tLaneList.size() > 0) {
                        tBaseElement = tLaneList.get(0);
                    }
                } else {
                    tBaseElement = endEventGroup.getKey();
                }
                if (tBaseElement != null) {

                    TEndEvent tEndEvent = createEndEvent(tProcess, tBaseElement);
                    TExclusiveGateway tExclusiveGateway = createExclusiveGateway(tProcess, tBaseElement);
                    createSequenceFlow(tExclusiveGateway, tEndEvent, tProcess);

                    List<TFlowElement> tFlowElementToRemoveList = new ArrayList<>();
                    for (TEndEvent tEndEventOld : tEndEventListOld) {
                        for (QName qNameEndEventOld : tEndEventOld.getIncoming()) {
                            String idIncoming = qNameEndEventOld.getLocalPart();
                            TSequenceFlow tSequenceFlowOld = (TSequenceFlow) bpmnWrapper.getFlowElementById(idIncoming);
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
    }

    private void removeElements(List<TFlowElement> flowElementList) {
        Iterator<TFlowElement> flowElementIterator = flowElementList.iterator();
        while (flowElementIterator.hasNext()) {
            TFlowElement flowElementToRemove = flowElementIterator.next();
            if (flowElementToRemove instanceof TSequenceFlow) {
                bpmnWrapper.deleteSequenceFlow((TSequenceFlow) flowElementToRemove);
            } else {
                bpmnWrapper.deleteFlowElement(flowElementToRemove);
            }
        }
    }

    private TStartEvent createStartEvent(TProcess tProcess, TBaseElement tBaseElement) {
        TStartEvent tStartEvent = new TStartEvent();
        tStartEvent.setId(generateId("StartEvent"));
        positionFlowNode(tProcess, tBaseElement, tStartEvent, new ObjectFactory().createStartEvent(tStartEvent));

        return tStartEvent;
    }

    private TEndEvent createEndEvent(TProcess tProcess, TBaseElement tBaseElement) {
        TEndEvent tEndEvent = new TEndEvent();
        tEndEvent.setId(generateId("EndEvent"));
        positionFlowNode(tProcess, tBaseElement, tEndEvent, new ObjectFactory().createEndEvent(tEndEvent));

        return tEndEvent;
    }

    private TExclusiveGateway createExclusiveGateway(TProcess tProcess, TBaseElement tBaseElement) {
        TExclusiveGateway tExclusiveGateway = new TExclusiveGateway();
        tExclusiveGateway.setId(generateId("ExclusiveGateway"));
        positionFlowNode(tProcess, tBaseElement, tExclusiveGateway, new ObjectFactory().createExclusiveGateway(tExclusiveGateway));

        return tExclusiveGateway;
    }

    private void createSequenceFlow(TFlowNode sourceNode, TFlowNode targetNode, TProcess tProcess) {
        TSequenceFlow tSequenceFlow = new TSequenceFlow();
        tSequenceFlow.setId(generateId("SequenceFlow"));

        TSubProcess tSubProcess = bpmnWrapper.getSubprocessByFlowElement(sourceNode);
        if (tSubProcess != null) {
            tSubProcess.getFlowElement().add(new ObjectFactory().createSequenceFlow(tSequenceFlow));
        } else {
            tProcess.getFlowElement().add(new ObjectFactory().createSequenceFlow(tSequenceFlow));
        }

        if (sourceNode != null) {
            tSequenceFlow.setSourceRef(sourceNode);
            sourceNode.getOutgoing().add(JaxbWrapper.getQName(tSequenceFlow.getClass(), tSequenceFlow));
        }

        if (targetNode != null) {
            tSequenceFlow.setTargetRef(targetNode);
            targetNode.getIncoming().add(JaxbWrapper.getQName(tSequenceFlow.getClass(), tSequenceFlow));
        }
    }

    private void positionFlowNode(TProcess tProcess, TBaseElement tBaseElement, TFlowNode tFlowNode, JAXBElement<? extends TFlowElement> jaxbFlowNode) {
        if (tBaseElement instanceof TLane) {
            tProcess.getFlowElement().add(jaxbFlowNode);
            ((TLane) tBaseElement).getFlowNodeRef().add(new ObjectFactory().createTLaneFlowNodeRef(tFlowNode));
        } else if (tBaseElement instanceof TSubProcess) {
            ((TSubProcess) tBaseElement).getFlowElement().add(jaxbFlowNode);
        }
    }

    private void adjustLabel() {
        for (TProcess process : bpmnWrapper.getProcessList()) {
            for (TFlowElement tFlowElement : bpmnWrapper.getFlowElementListDeep(process)) {
                if (tFlowElement != null) {
                    String name = tFlowElement.getName();
                    if (name != null) {
                        tFlowElement.setName(tFlowElement.getName().replaceAll("\n", " ").replaceAll("  ", " ").trim());
                    }
                }
            }
        }
    }

    private void adjustActivityLabel() {
        for (TProcess process : bpmnWrapper.getProcessList()) {
            List<TActivity> tActivityList = bpmnWrapper.getFlowElementListDeep(TActivity.class, process);
            for (TActivity tActivity : tActivityList) {
                String name = tActivity.getName();
                if (name != null) {
                    name = name.trim();
                }

                if (name == null || name.isEmpty()) {
                    String type = "activity";
                    if (tActivity instanceof TTask) {
                        type = "task";
                    } else if (tActivity instanceof TSubProcess) {
                        type = "subprocess";
                    }
                    String label = ("Do unlabeled @type (id: " + tActivity.getId() + ")\n").replace("@type", type);
                    tActivity.setName(label);
                }
            }
        }
    }

    private String generateId(String idName) {
        int idValue = 1;
        List<String> idList = bpmnWrapper.getElementsId();

        String newId;
        do {
            newId = idName + " " + idValue;
            idValue++;
        } while (idList.contains(newId));
        return newId;
    }

    private String generateName(String name) {
        int idValue = 1;
        List<String> idList = bpmnWrapper.getElementsName();

        String newName;
        do {
            newName = name + " " + idValue;
            idValue++;
        } while (idList.contains(newName));
        return newName;
    }

}
