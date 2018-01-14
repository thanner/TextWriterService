package br.edu.ufrgs.inf.bpm.wrapper;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import processToText.dataModel.process.*;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ProcessModelXmlBuilder {

    private int genericId;
    private Map<String, Lane> laneMap;
    private Map<String, Pool> poolMap;
    private Map<String, Element> elementMap;
    private Map<String, Integer> idMap;
    private BpmnXmlWrapper bpmnXmlWrapper;

    public ProcessModelXmlBuilder() {
        genericId = 0;
        laneMap = new HashMap<>();
        poolMap = new HashMap<>();
        elementMap = new HashMap<>();
        idMap = new HashMap<>();
    }

    public ProcessModel buildProcess(String bpmnString) {
        int newId = generateModelId("ProcessModel1");
        ProcessModel processModel = new ProcessModel(newId, "Process Model");

        try {
            bpmnXmlWrapper = new BpmnXmlWrapper(bpmnString);
            bpmnXmlWrapper.getModelElementsByType(LaneSet.class).forEach(p -> processModel.addPool(createPool(p)));
            bpmnXmlWrapper.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Lane.class).forEach(p -> processModel.addLane(createLane(p)));
            bpmnXmlWrapper.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Activity.class).forEach(p -> processModel.addActivity(createActivity(p)));
            bpmnXmlWrapper.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Event.class).forEach(p -> processModel.addEvent(createEvent(p)));
            bpmnXmlWrapper.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Gateway.class).forEach(p -> processModel.addGateway(createGateway(p)));
            bpmnXmlWrapper.getModelElementsByType(SequenceFlow.class).forEach(p -> processModel.addArc(createArc(p)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return processModel;
    }

    private String createPool(org.camunda.bpm.model.bpmn.instance.LaneSet pool) {
        int newId = generateModelId(pool.getId());
        Pool modelPool = new Pool(newId, pool.getName());
        poolMap.put(pool.getId(), modelPool);
        return modelPool.getName();
    }

    private String createLane(org.camunda.bpm.model.bpmn.instance.Lane lane) {
        int newId = generateModelId(lane.getId());
        Lane modelLane = new Lane(newId, lane.getName(), getPoolByLane(lane));
        laneMap.put(lane.getId(), modelLane);
        return modelLane.getName();
    }

    private Activity createActivity(org.camunda.bpm.model.bpmn.instance.Activity activity) {
        try {
            int newId = generateModelId(activity.getId());
            int activityType = getActivityType(activity.getElementType().getTypeName());
            Activity modelActivity = new Activity(newId, activity.getName(), getLaneByObject(activity), getPoolByObject(activity), activityType);
            elementMap.put(activity.getId(), modelActivity);
            return modelActivity;
        } catch (InvalidObjectException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Event createEvent(org.camunda.bpm.model.bpmn.instance.Event event) {
        try {
            int newId = generateModelId(event.getId());
            int eventType = getEventType(event.getElementType().getTypeName());
            Event modelEvent = new Event(newId, event.getName(), getLaneByObject(event), getPoolByObject(event), eventType);
            elementMap.put(event.getId(), modelEvent);
            return modelEvent;
        } catch (InvalidObjectException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Gateway createGateway(org.camunda.bpm.model.bpmn.instance.Gateway gateway) {
        try {
            int newId = generateModelId(gateway.getId());
            int gatewayType = getGatewayType(gateway.getElementType().getTypeName());
            Gateway modelGateway = new Gateway(newId, gateway.getName(), getLaneByObject(gateway), getPoolByObject(gateway), gatewayType);
            elementMap.put(gateway.getId(), modelGateway);
            return modelGateway;
        } catch (InvalidObjectException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Arc createArc(org.camunda.bpm.model.bpmn.instance.SequenceFlow arc) {
        int newId = generateModelId(arc.getId());
        return new Arc(newId, arc.getName(), elementMap.get(arc.getSource().getId()), elementMap.get(arc.getTarget().getId()));
    }

    private int getActivityType(String typeName) throws InvalidObjectException {
        switch (typeName) {
            case "task":
                return 0;
            case "subProcess":
                return 3;
            default:
                throw new InvalidObjectException(typeName);
        }
    }

    private int getEventType(String typeName) throws InvalidObjectException {
        switch (typeName) {
            case "startEvent":
                return 1;
            case "endEvent":
                return 11;
            default:
                throw new InvalidObjectException(typeName);
        }

        /*
            public static final int START_EVENT = 1;
            public static final int START_MSG = 2;
            public static final int START_TIMER = 3;

            public static final int END_EVENT = 11;
            public static final int END_ERROR = 12;
            public static final int END_SIGNAL = 13;
            public static final int END_MSG = 14;

            public static final int INTM = 21;
            public static final int INTM_TIMER = 22;
            public static final int INTM_CANCEL = 23;
            public static final int INTM_CONDITIONAL = 24;
            public static final int INTM_ESCALATION = 25;
            public static final int INTM_ERROR = 26;

            public static final int INTM_ESCALATION_THR = 31;
            public static final int INTM_SIGNAL_THR = 32;
            public static final int INTM_MULTIPLE_THR = 33;
            public static final int INTM_LINK_THR = 34;
            public static final int INTM_MSG_THR = 35;

            public static final int INTM_ESCALATION_CAT = 41;
            public static final int INTM_MULTIPLE_CAT = 42;
            public static final int INTM_LINK_CAT = 43;
            public static final int INTM_MSG_CAT = 44;
            public static final int INTM_PMULT_CAT = 45;
            public static final int INTM_COMPENSATION_CAT = 46;
         */
    }

    private int getGatewayType(String typeName) throws InvalidObjectException {
        switch (typeName) {
            case "exclusiveGateway":
                return 0;
            case "inclusiveGateway":
                return 1;
            case "parallelGateway":
                return 2;
            case "eventBasedGateway":
                return 3;
            default:
                throw new InvalidObjectException(typeName);
        }
    }

    private Pool getPoolByLane(org.camunda.bpm.model.bpmn.instance.Lane lane) {
        ModelElementInstance parent = lane.getParentElement();
        if (parent.getElementType().getTypeName().equalsIgnoreCase(LaneSet.class.getSimpleName())) {
            LaneSet laneSet = (LaneSet) parent;
            return poolMap.get(laneSet.getId());
        } else {
            return null;
        }
    }

    private Pool getPoolByObject(FlowNode flowNode) {
        LaneSet pool = bpmnXmlWrapper.getPoolByFlowNode(flowNode);
        if (pool != null) {
            return poolMap.get(pool.getId());
        } else {
            return null;
        }
    }

    private Lane getLaneByObject(FlowNode flowNode) {
        org.camunda.bpm.model.bpmn.instance.Lane lane = bpmnXmlWrapper.getLaneByFlowNode(flowNode);
        if (lane != null) {
            return laneMap.get(lane.getId());
        } else {
            return null;
        }
    }

    private int generateModelId(String oldId) {
        int newId = genericId++;
        idMap.put(oldId, newId);
        return newId;
    }

    public Map<String,Integer> getIdMap() {
        return idMap;
    }
}
