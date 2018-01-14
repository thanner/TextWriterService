package br.edu.ufrgs.inf.bpm.wrapper;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BpmnXmlWrapper {

    public BpmnModelInstance modelInstance;
    public Definitions definitions;
    private Map<String, Process> processMap;
    private Map<String, LaneSet> laneSetMap;
    private Map<String, String> elementMap;
    private Collaboration collaboration;

    public BpmnXmlWrapper() {
        this.modelInstance = Bpmn.createEmptyModel();
        definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        init();
        collaboration = createElement(definitions, "Collaboration", "", Collaboration.class);
    }

    public BpmnXmlWrapper(InputStream bpmnStream) {
        this.modelInstance = Bpmn.readModelFromStream(bpmnStream);
        definitions = modelInstance.getDefinitions();
        init();
    }

    public BpmnXmlWrapper(String bpmnString) throws UnsupportedEncodingException {
        this.modelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(bpmnString.getBytes(StandardCharsets.UTF_8.name())));
        definitions = modelInstance.getDefinitions();
        init();
    }

    private void init() {
        this.processMap = new HashMap<>();
        this.laneSetMap = new HashMap<>();
        this.elementMap = new HashMap<>();
    }

    public Participant createPool(String id, String text) {
        Participant participant = createElement(collaboration, "pa" + id, text, Participant.class);
        collaboration.getParticipants().add(participant);

        Process process = createElement(definitions, "po" + id, text, Process.class);
        processMap.put(getElementId("po" + id, Process.class), process);
        participant.setProcess(process);

        LaneSet laneSet = createElement(process, "l" + id, "", LaneSet.class);
        laneSetMap.put(getElementId("l" + id, Process.class), laneSet);

        return participant;
    }

    public <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, String name, Class<T> elementClass) {
        T element = modelInstance.newInstance(elementClass);
        if (!id.isEmpty()) {
            element.setAttributeValue("id", getElementId(id, elementClass), true);
        }
        if (!name.isEmpty()) {
            element.setAttributeValue("name", name);
        }
        parentElement.addChildElement(element);
        return element;
    }

    public SequenceFlow createSequenceFlow(Process process, String name, FlowNode from, FlowNode to) {
        SequenceFlow sequenceFlow = createElement(process, from.getId() + "-" + to.getId(), name, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }

    public FlowNode getElementById(String id) {
        return modelInstance.getModelElementById(elementMap.get(id));
    }

    public <T extends ModelElementInstance> Collection<T> getModelElementsByType(Class<T> elementType) {
        return modelInstance.getModelElementsByType(elementType);
    }

    public Process getProcess(String id) {
        return processMap.get(getElementId("po" + id, Process.class));
    }

    public LaneSet getLaneSet(String id) {
        return laneSetMap.get(getElementId("l" + id, LaneSet.class));
    }

    private String getElementId(String id, Class element) {
        if (!elementMap.containsKey(id)) {
            elementMap.put(id, element.getSimpleName() + "-" + id);
        }
        return elementMap.get(id);
    }

    public LaneSet getPoolByFlowNode(FlowNode flowNode) {
        ModelElementInstance parent = flowNode.getParentElement();
        if (parent.getElementType().getTypeName().equalsIgnoreCase(Process.class.getSimpleName())) {
            Process process = (Process) parent;
            for (LaneSet laneSet : process.getLaneSets()) {
                for (Lane lane : laneSet.getLanes()) {
                    for (FlowNode flowNodeRef : lane.getFlowNodeRefs()) {
                        if (flowNode.getId().equals(flowNodeRef.getId())) {
                            return laneSet;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Lane getLaneByFlowNode(FlowNode flowNode) {
        ModelElementInstance parent = flowNode.getParentElement();
        if (parent.getElementType().getTypeName().equalsIgnoreCase(Process.class.getSimpleName())) {
            Process process = (Process) parent;
            for (LaneSet laneSet : process.getLaneSets()) {
                for (Lane lane : laneSet.getLanes()) {
                    for (FlowNode flowNodeRef : lane.getFlowNodeRefs()) {
                        if (flowNode.getId().equals(flowNodeRef.getId())) {
                            return lane;
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getBpmnXmlString() {
        return Bpmn.convertToString(modelInstance);
    }

    public InputStream getBpmnXmlInputStream() {
        try {
            return new ByteArrayInputStream(getBpmnXmlString().getBytes(StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ModelInstance read(String filePath) {
        return Bpmn.readModelFromFile(new File(filePath));
    }

    public void writeToFile(String pathname) {
        File file = new File(pathname);
        Bpmn.writeModelToFile(file, modelInstance);
    }
}
