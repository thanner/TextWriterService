package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.*;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.ActivityType;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.EventType;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.GatewayType;
import processToText.dataModel.process.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessModelBuilder {

    private int genericId;
    private Map<Integer, String> bpmnIdMap;

    private Map<String, Lane> laneMap;
    private Map<String, Pool> poolMap;
    private Map<String, Element> elementMap;
    private Map<String, Arc> tArcMap;
    private Map<Integer, Arc> arcMap;

    private BpmnWrapper processModelWrapper;
    private Map<Integer, Integer> externalPathInitiators;

    public ProcessModelBuilder() {
        genericId = 0;
        bpmnIdMap = new HashMap<>();

        laneMap = new HashMap<>();
        poolMap = new HashMap<>();
        elementMap = new HashMap<>();
        tArcMap = new HashMap<>();
        arcMap = new HashMap<>();

        externalPathInitiators = new HashMap<>();
    }

    public ProcessModel buildProcess(TDefinitions definitions) {
        processModelWrapper = new BpmnWrapper(definitions);

        int newId = generateModelId("ProcessModel1");
        ProcessModel processModel = new ProcessModel(newId, "Process Model");

        List<TProcess> processList = processModelWrapper.getProcessList();

        for (TProcess process : processList) {

            processModel.addPool(createPool(process));
            for (TLaneSet laneSet : process.getLaneSet()) {
                for (TLane lane : laneSet.getLane()) {
                    processModel.addLane(createLane(lane, process));
                }
            }

            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TActivity) {
                    processModel.addActivity(createActivity((TActivity) flowElement.getValue()));
                } else if (flowElement.getValue() instanceof TEvent) {
                    processModel.addEvent(createEvent((TEvent) flowElement.getValue()));
                } else if (flowElement.getValue() instanceof TGateway) {
                    processModel.addGateway(createGateway((TGateway) flowElement.getValue()));
                }
            }

            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TSequenceFlow) {
                    processModel.addArc(createArc((TSequenceFlow) flowElement.getValue()));
                }
            }

            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TBoundaryEvent) {
                    attachEvent(process, processModel, (TBoundaryEvent) flowElement.getValue());
                }
            }

            removeExternalPathInitiators(process, processModel);
        }

        // TODO: verificar build alternative path model
        return processModel;
    }

    private String createPool(TProcess process) {
        int newId = generateModelId(process.getId());
        Pool modelPool = new Pool(newId, processModelWrapper.getProcessName(process));
        poolMap.put(process.getId(), modelPool);
        return modelPool.getName();
    }

    private String createLane(TLane lane, TProcess process) {
        int newId = generateModelId(lane.getId());
        Lane modelLane = new Lane(newId, getName(lane.getName()), poolMap.get(process.getId()));
        laneMap.put(lane.getId(), modelLane);
        return modelLane.getName();
    }

    private Activity createActivity(TActivity activity) {
        try {
            int newId = generateModelId(activity.getId());
            int activityType = getActivityType(activity);
            Activity modelActivity = new Activity(newId, getName(activity.getName()), getLaneByObject(activity), getPoolByObject(activity), activityType);
            elementMap.put(activity.getId(), modelActivity);
            return modelActivity;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Event createEvent(TEvent event) {
        try {
            int newId = generateModelId(event.getId());
            int eventType = getEventType(event);
            Event modelEvent = new Event(newId, getName(event.getName()), getLaneByObject(event), getPoolByObject(event), eventType);
            elementMap.put(event.getId(), modelEvent);
            return modelEvent;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Gateway createGateway(TGateway gateway) {
        try {
            int newId = generateModelId(gateway.getId());
            int gatewayType = getGatewayType(gateway);
            Gateway modelGateway = new Gateway(newId, getName(gateway.getName()), getLaneByObject(gateway), getPoolByObject(gateway), gatewayType);
            elementMap.put(gateway.getId(), modelGateway);
            return modelGateway;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Arc createArc(TSequenceFlow arc) {
        int newId = generateModelId(arc.getId());
        Arc modelArc = new Arc(newId, getName(arc.getName()), elementMap.get(((TFlowNode) arc.getSourceRef()).getId()), elementMap.get(((TFlowNode) arc.getTargetRef()).getId()));
        arcMap.put(newId, modelArc);
        tArcMap.put(arc.getId(), modelArc);
        return modelArc;
    }

    private void attachEvent(TProcess process, ProcessModel processModel, TBoundaryEvent tBoundaryEvent) {
        String tActivityId = tBoundaryEvent.getAttachedToRef().getLocalPart();
        Activity activity = (Activity) elementMap.get(tActivityId);
        Event event = (Event) elementMap.get(tBoundaryEvent.getId());

        activity.addAttachedEvent(event.getId());
        event.setAttached(true);
        event.setIsAttachedTo(activity.getId());

        TActivity tActivity = getTActivityAttached(process, tActivityId);
        if (tActivity != null) {
            // if (tActivity.getOutgoing().size() > 1) {
            // Attached event leads to alternative path
            externalPathInitiators.put(event.getId(), activity.getId());
            // } else {
            // Attached event goes back to standard path
            //     Arc arc = new Arc(generateModelId("newArc" + genericId), "", activity, event, "VirtualFlow");
            //    processModel.addArc(arc);
            // }
        }
    }

    private String getName(String name) {
        return name != null ? name : "";
    }

    private int getActivityType(TActivity activity) throws IllegalArgumentException {
        try {
            return ActivityType.valueOf(activity.getClass().getSimpleName()).getValue();
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(activity);
        }
    }

    private int getEventType(TEvent event) throws IllegalArgumentException {
        try {
            EventType eventType = new EventType();
            return eventType.getEventType(event);
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(event);
        }
    }

    private int getGatewayType(TGateway gateway) throws IllegalArgumentException {
        try {
            return GatewayType.valueOf(gateway.getClass().getSimpleName()).getValue();
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(gateway);
        }
    }

    private IllegalArgumentException getIllegalTypeException(TFlowNode flowNode) {
        return new IllegalArgumentException("Can not find element type (Element: " + flowNode.getClass().getSimpleName() + ". Id: " + flowNode.getId() + ")");
    }

    private Pool getPoolByObject(TFlowNode flowNode) {
        TProcess process = processModelWrapper.getProcessByFlowNode(flowNode);
        return process != null ? poolMap.get(process.getId()) : null;
    }

    private Lane getLaneByObject(TFlowNode flowNode) {
        TLane lane = processModelWrapper.getLaneByFlowNode(flowNode);
        return lane != null ? laneMap.get(lane.getId()) : null;
    }

    private int generateModelId(String oldId) {
        int newId = genericId++;
        bpmnIdMap.put(newId, oldId);
        return newId;
    }

    public Map<Integer, String> getBpmnIdMap() {
        return bpmnIdMap;
    }

    private void removeExternalPathInitiators(TProcess tProcess, ProcessModel processModel) {
        for (int exPI : externalPathInitiators.keySet()) {
            ProcessModel alternativePathModel = new ProcessModel(exPI, "");

            // Create start event
            Event startEvent = new Event(generateModelId("newStartEvent" + genericId), "", processModel.getElem(exPI).getLane(), processModel.getElem(exPI).getPool(), processToText.dataModel.process.EventType.START_EVENT);
            alternativePathModel.addEvent(startEvent);

            // Reallocate elems to alternative path
            buildAlternativePathModel(tProcess, exPI, true, processModel, alternativePathModel, exPI);

            // Add arc from artifical start to real start elem
            Event realStart = (Event) alternativePathModel.getElem(exPI);
            alternativePathModel.addArc(new Arc(generateModelId("newArc" + genericId), "", startEvent, realStart));

            // Add path to model
            processModel.addAlternativePath(alternativePathModel, exPI);

        }
    }

    private TActivity getTActivityAttached(TProcess process, String tActivityId) {
        TActivity tActivity;
        for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
            if (flowElement.getValue() instanceof TActivity) {
                tActivity = (TActivity) flowElement.getValue();
                if (tActivity.getId().equals(tActivityId)) {
                    return tActivity;
                }
            }
        }
        return null;
    }

    private void buildAlternativePathModel(TProcess tProcess, int id, boolean isElement, ProcessModel model, ProcessModel alternative, int exPI) {
        if (isElement) {
            TFlowNode tFlowNode = getTFlowNode(tProcess, id);
            if (tFlowNode.getOutgoing().size() > 0) {
                for (QName qName : tFlowNode.getOutgoing()) {
                    String idOutgoing = qName.getLocalPart();
                    int idArc = tArcMap.get(idOutgoing).getId();
                    buildAlternativePathModel(tProcess, idArc, false, model, alternative, exPI);

                    Element element = model.getElem(id);
                    if (element != null) {
                        alternative.addElem(element);
                        // elementMap.remove(id);
                        model.removeElem(id);
                    }
                    // System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
                }
            } else {
                Element element = model.getElem(id);
                if (element != null) {
                    alternative.addElem(element);
                    // elementMap.remove(id);
                    model.removeElem(id);
                }
                // System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
            }
        } else {
            buildAlternativePathModel(tProcess, arcMap.get(id).getTarget().getId(), true, model, alternative, exPI);

            Arc arc = model.getArc(id);
            if (arc != null) {
                alternative.addArc(arc);
                //arcMap.remove(id);
                model.removeArc(id);
            }
            // System.out.println("Arc reallocated: " + id + " --> " + exPI);
        }
    }

    private TFlowNode getTFlowNode(TProcess tProcess, int id) {
        for (JAXBElement<? extends TFlowElement> currentFlowElement : tProcess.getFlowElement()) {
            if (currentFlowElement.getValue() instanceof TFlowNode) {
                TFlowNode tFlowNode = (TFlowNode) currentFlowElement.getValue();
                if (id == elementMap.get(tFlowNode.getId()).getId()) {
                    return tFlowNode;
                }
            }
        }
        return null;
    }

}