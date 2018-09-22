package br.edu.ufrgs.inf.bpm.wrapper;

import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class BpmnWrapper {

    private TDefinitions definitions;

    public BpmnWrapper(TDefinitions definitions) {
        this.definitions = definitions;
    }

    public List<TLane> getLanesByProcess(TProcess tProcess) {
        List<TLane> tLaneList = new ArrayList<>();
        for (TLaneSet laneSet : tProcess.getLaneSet()) {
            for (TLane lane : laneSet.getLane()) {
                tLaneList.add(lane);
            }
        }
        return tLaneList;
    }

    public TProcess getProcessByFlowElement(TFlowElement tFlowElement) {
        List<TProcess> processList = getProcessList();
        for (TProcess process : processList) {
            for (TLaneSet laneSet : process.getLaneSet()) {
                for (TLane lane : laneSet.getLane()) {
                    for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                        if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                            TFlowNode flowNodeAux = (TFlowNode) flowNodeRefObject.getValue();
                            if (tFlowElement.getId().equals(flowNodeAux.getId())) {
                                return process;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public TLane getLaneByFlowElement(TFlowElement tFlowElement) {
        List<TProcess> processList = getProcessList();
        for (TProcess process : processList) {
            for (TLaneSet laneSet : process.getLaneSet()) {
                for (TLane lane : laneSet.getLane()) {
                    for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                        if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                            TFlowNode flowNodeAux = (TFlowNode) flowNodeRefObject.getValue();
                            if (tFlowElement.getId().equals(flowNodeAux.getId())) {
                                return lane;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<TCollaboration> getCollaborationList() {
        List<TCollaboration> collaborationList = new ArrayList<>();
        List<JAXBElement<? extends TRootElement>> rootElementList = definitions.getRootElement();
        for (JAXBElement<? extends TRootElement> rootElement : rootElementList) {
            if (rootElement.getValue() instanceof TCollaboration) {
                TCollaboration collaboration = (TCollaboration) rootElement.getValue();
                collaborationList.add(collaboration);
            }
        }
        return collaborationList;
    }

    public List<TProcess> getProcessList() {
        List<TProcess> processList = new ArrayList<>();

        List<JAXBElement<? extends TRootElement>> rootElementList = definitions.getRootElement();
        for(JAXBElement<? extends TRootElement> root : rootElementList){
            if (root.getValue() instanceof TProcess) {
                TProcess process = (TProcess) root.getValue();
                processList.add(process);
            }
        }
        return processList;
    }

    public String getProcessName(TProcess process) {
        List<TCollaboration> collaborationList = getCollaborationList();
        for (TCollaboration collaboration : collaborationList) {
            for (TParticipant participant : collaboration.getParticipant()) {
                if (process.getId().equals(participant.getProcessRef().toString())) {
                    return participant.getName();
                }
            }
        }
        return "";
    }

    public TParticipant getParticipantFromProcess(TProcess tProcess) {
        List<TCollaboration> collaborationList = getCollaborationList();
        for (TCollaboration collaboration : collaborationList) {
            for (TParticipant participant : collaboration.getParticipant()) {
                if (tProcess.getId().equals(participant.getProcessRef().toString())) {
                    return participant;
                }
            }
        }
        return null;
    }

        /*
    public TProcess getProcessByFlowNode(TFlowNode flowNode) {
        List<TProcess> processList = getProcessList();
        for (TProcess process : processList) {
            for (TLaneSet laneSet : process.getLaneSet()) {
                for (TLane lane : laneSet.getLane()) {
                    for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                        if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                            TFlowNode flowNodeAux = (TFlowNode) flowNodeRefObject.getValue();
                            if (flowNode.getId().equals(flowNodeAux.getId())) {
                                return process;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    */

    /*
    public TLane getLaneByFlowNode(TFlowNode flowNode) {
        List<TProcess> processList = getProcessList();
        for (TProcess process : processList) {
            for (TLaneSet laneSet : process.getLaneSet()) {
                for (TLane lane : laneSet.getLane()) {
                    for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                        if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                            TFlowNode flowNodeAux = (TFlowNode) flowNodeRefObject.getValue();
                            if (flowNode.getId().equals(flowNodeAux.getId())) {
                                return lane;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    */

    public boolean hasParticipant(TProcess process) {
        List<TCollaboration> collaborationList = getCollaborationList();
        for (TCollaboration collaboration : collaborationList) {
            for (TParticipant participant : collaboration.getParticipant()) {
                if (process.getId().equals(participant.getProcessRef().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    public List<TSequenceFlow> getSequenceFlowList() {
        List<TSequenceFlow> tSequenceFlowList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                if (flowElement.getValue() instanceof TSequenceFlow) {
                    tSequenceFlowList.add((TSequenceFlow) flowElement.getValue());
                }
            }
        }
        return tSequenceFlowList;
    }

    public List<TActivity> getActivityList() {
        List<TActivity> tActivityList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                if (flowElement.getValue() instanceof TActivity) {
                    tActivityList.add((TActivity) flowElement.getValue());
                }
            }
        }
        return tActivityList;
    }
    */

    public <T> List<T> getFlowElementList(Class flowElementClass){
        List<T> elementList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            elementList.addAll(getFlowElementList(flowElementClass, tProcess));
        }
        return elementList;
    }

    public <T> List<T> getFlowElementList(Class<T> flowElementClass, TProcess tProcess){
        List<T> elementList = new ArrayList<>();
        for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
            if (flowElementClass.isInstance(flowElement.getValue())) {
                elementList.add((T) flowElement.getValue());
            }
        }
        return elementList;
    }

    public List<TFlowElement> getFlowElementList() {
        List<TFlowElement> elementList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                elementList.add(flowElement.getValue());
            }
        }
        return elementList;
    }

    public List<TFlowNode> getFlowNodesWithoutIncoming(TProcess tProcess){
        List<TFlowNode> flowNodeWithoutIncomingList = new ArrayList();
        for (TLaneSet laneSet : tProcess.getLaneSet()) {
            for (TLane lane : laneSet.getLane()) {
                for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                    if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                        TFlowNode tFlowNode = (TFlowNode) flowNodeRefObject.getValue();
                        if(tFlowNode.getIncoming() == null || tFlowNode.getIncoming().isEmpty()){
                            flowNodeWithoutIncomingList.add(tFlowNode);
                        }
                    }
                }
            }
        }
        return flowNodeWithoutIncomingList;
    }

    public List<TFlowNode> getFlowNodesWithoutOutgoing(TProcess tProcess){
        List<TFlowNode> flowNodeWithoutOutgoingList = new ArrayList();
        for (TLaneSet laneSet : tProcess.getLaneSet()) {
            for (TLane lane : laneSet.getLane()) {
                for (JAXBElement<Object> flowNodeRefObject : lane.getFlowNodeRef()) {
                    if (flowNodeRefObject.getValue() instanceof TFlowNode) {
                        TFlowNode tFlowNode = (TFlowNode) flowNodeRefObject.getValue();
                        if(tFlowNode.getOutgoing() == null || tFlowNode.getOutgoing().isEmpty()){
                            flowNodeWithoutOutgoingList.add(tFlowNode);
                        }
                    }
                }
            }
        }
        return flowNodeWithoutOutgoingList;
    }

    /*
    private TFLowElement getFlowElementById(String flowElementId) {
    }
    */

    public TFlowElement getFlowElementById(String flowElementId) {
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                if (flowElement.getValue().getId().equals(flowElementId)) {
                    return flowElement.getValue();
                }
            }
        }
        return null;
    }

    public <T> T getFlowElementById(Class<T> flowElementClass, String flowElementId) {
        for(TProcess tProcess: getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement: tProcess.getFlowElement()){
                if (flowElement.getValue().getId().equals(flowElementId)) {
                    if (flowElementClass.isInstance(flowElement.getValue())) {
                        return (T) flowElement.getValue();
                    }
                }
            }
        }
        return null;
    }

    public void deleteFlowElementById(String flowElementId) {
        for (TProcess tProcess : getProcessList()) {
            tProcess.getFlowElement().removeIf(flowElement -> flowElement.getValue().getId() != null && flowElement.getValue().getId().equals(flowElementId));

            for (TLaneSet tLaneSet : tProcess.getLaneSet()) {
                for (TLane tLane : tLaneSet.getLane()) {
                    tLane.getFlowNodeRef().removeIf(tFlowNodeRef -> {
                        if (tFlowNodeRef.getValue() instanceof TFlowElement) {
                            TFlowElement tFlowElement = (TFlowElement) tFlowNodeRef.getValue();
                            if (tFlowElement.getId() != null && tFlowElement.getId().equals(flowElementId)) {
                                return true;
                            }
                        }
                        return false;
                    });
                }
            }

        }
    }

    public void deleteFlowElement(TFlowElement flowElementToRemove) {
        deleteFlowElementById(flowElementToRemove.getId());
    }

    public void deleteSequenceFlowById(String flowElementId) {
        TSequenceFlow tSequenceFlow = getFlowElementById(TSequenceFlow.class, flowElementId);
        deleteSequenceFlow(tSequenceFlow);
    }

    public void deleteSequenceFlow(TSequenceFlow tSequenceFlow) {
        if (tSequenceFlow.getSourceRef() instanceof TFlowNode) {
            TFlowNode source = (TFlowNode) tSequenceFlow.getSourceRef();
            removeReference(tSequenceFlow.getId(), source.getOutgoing());
        }

        if (tSequenceFlow.getTargetRef() instanceof TFlowNode) {
            TFlowNode target = (TFlowNode) tSequenceFlow.getTargetRef();
            removeReference(tSequenceFlow.getId(), target.getIncoming());
        }

        deleteFlowElementById(tSequenceFlow.getId());
    }

    public void removeReference(String elementOfReference, List<QName> referenceList) {
        referenceList.removeIf(reference -> reference.getLocalPart().equals(elementOfReference));
    }

    public List<String> getElementsId() {
        List<String> idList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                idList.add(flowElement.getValue().getId());
            }
        }
        return idList;
    }

    public List<String> getElementsName() {
        List<String> nameList = new ArrayList<>();
        for (TProcess tProcess : getProcessList()) {
            for (JAXBElement<? extends TFlowElement> flowElement : tProcess.getFlowElement()) {
                nameList.add(flowElement.getValue().getName());
            }
        }
        return nameList;
    }

    public List<TFlowElement> getFlowNodeSourceList(TFlowNode tFlowNode) {
        List<TFlowElement> elementSourceList = new ArrayList<>();
        for (QName qName : tFlowNode.getIncoming()) {
            TFlowElement tFlowElement = getFlowElementById(qName.getLocalPart());
            if (tFlowElement instanceof TSequenceFlow) {
                TSequenceFlow tSequenceFlow = (TSequenceFlow) tFlowElement;
                Object source = tSequenceFlow.getSourceRef();
                if (source instanceof TFlowElement) {
                    elementSourceList.add((TFlowElement) source);
                }
            }
        }
        return elementSourceList;
    }

    public List<TFlowElement> getFlowNodeTargetList(TFlowNode tFlowNode) {
        List<TFlowElement> elementTargetList = new ArrayList<>();
        for (QName qName : tFlowNode.getOutgoing()) {
            TFlowElement tFlowElement = getFlowElementById(qName.getLocalPart());
            if (tFlowElement instanceof TSequenceFlow) {
                TSequenceFlow tSequenceFlow = (TSequenceFlow) tFlowElement;
                Object target = tSequenceFlow.getTargetRef();
                if (target instanceof TFlowElement) {
                    elementTargetList.add((TFlowElement) target);
                }
            }
        }
        return elementTargetList;
    }

    public boolean isGatewaySplit(TGateway tGateway) {
        return tGateway.getIncoming().size() < tGateway.getOutgoing().size();
    }

    public boolean isGatewayJoin(TGateway tGateway) {
        return tGateway.getIncoming().size() > tGateway.getOutgoing().size();
    }

    public TDefinitions getDefinitions() {
        return this.definitions;
    }

}