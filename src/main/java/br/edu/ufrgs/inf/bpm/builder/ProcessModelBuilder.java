package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.*;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.ActivityType;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.EventType;
import br.edu.ufrgs.inf.bpm.wrapper.elementType.GatewayType;
import processToText.dataModel.process.*;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessModelBuilder {

    private int genericId;
    private Map<Integer, String> bpmnIdMap;

    private Map<String, Lane> laneMap;
    private Map<String, Pool> poolMap;
    private Map<String, Element> elementMap;
    private BpmnWrapper processModelWrapper;
    private Map<Integer, Integer> externalPathInitiators;

    public ProcessModelBuilder() {
        genericId = 0;
        bpmnIdMap = new HashMap<>();

        laneMap = new HashMap<>();
        poolMap = new HashMap<>();
        elementMap = new HashMap<>();

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
                    Event event = createEvent((TEvent) flowElement.getValue());
                    //if(event.isAttached())
                    processModel.addEvent(event);
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
                    attachEvent((TBoundaryEvent) flowElement.getValue());
                }
            }
        }

        removeExternalPathInitiators(processModel);

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
        return new Arc(newId, getName(arc.getName()), elementMap.get(((TFlowNode) arc.getSourceRef()).getId()), elementMap.get(((TFlowNode) arc.getTargetRef()).getId()));
    }

    private void attachEvent(TBoundaryEvent tBoundaryEvent) {
        String activityId = tBoundaryEvent.getAttachedToRef().toString();
        Activity activity = (Activity) elementMap.get(activityId);
        activity.addAttachedEvent(elementMap.get(tBoundaryEvent.getId()).getId());

        Event event = (Event) elementMap.get(tBoundaryEvent.getId());
        event.setAttached(true);
        event.setIsAttachedTo(activity.getId());

        // TODO: Fazer
        /*
        // Iterate over all elems to create the according arcs
            for (JSONElem elem : elems.values()) {
                for (int outId : elem.getArcs()) {

                    // if considered outgoing id does not belong to an arc, create a new one (in order to connect attached event)
                    if (elems.containsKey(outId)) {
                        processToText.dataModel.process.Activity activity = ((processToText.dataModel.process.Activity) idMap.get(elem.getId()));
                        activity.addAttachedEvent(outId);

                        // Attached event leads to alternative path
                        if (elem.getArcs().size() > 1) {
                            System.out.println("Attached Event with alternative Path detected: " + elem.getLabel());
                            ((processToText.dataModel.process.Event) model.getElem(outId)).setIsAttachedTo(elem.getId());
                            ((processToText.dataModel.process.Event) model.getElem(outId)).setAttached(true);
                            externalPathInitiators.put(outId, elem.getId());

                            // Attached event goes back to standard path
                        } else {
                            processToText.dataModel.process.Arc arc = new processToText.dataModel.process.Arc(getId(), "", idMap.get(elem.getId()), idMap.get(outId), "VirtualFlow");
                            processToText.dataModel.process.Event attEvent = ((processToText.dataModel.process.Event) idMap.get(outId));
                            attEvent.setAttached(true);
                            attEvent.setIsAttachedTo(elem.getId());
                            model.addArc(arc);
                        }
                        // Considered outgoing id exists as arc
                    } else if (arcs.keySet().contains(outId)) {
                        JSONArc jArc = arcs.get(outId);
                        if (jArc.getType().equals("SequenceFlow")) {
                            processToText.dataModel.process.Arc arc = new processToText.dataModel.process.Arc(outId, jArc.getLabel(), idMap.get(elem.getId()), idMap.get(jArc.getTarget()), "SequenceFlow");
                            model.addArc(arc);
                        } else {
                            processToText.dataModel.process.Arc arc = new processToText.dataModel.process.Arc(outId, jArc.getLabel(), idMap.get(elem.getId()), idMap.get(jArc.getTarget()), "MessageFlow");
                            model.addArc(arc);
                        }
                    } else {
                        System.out.println("No according Arc found: " + outId);
                    }
                }
            }
         */
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

    private void removeExternalPathInitiators(ProcessModel processModel) {
        for (int exPI : externalPathInitiators.keySet()) {
            ProcessModel alternativePathModel = new ProcessModel(exPI, "");

            // Create start event
            processToText.dataModel.process.Event startEvent = new processToText.dataModel.process.Event(generateModelId("newStartEvent" + genericId), "", processModel.getElem(exPI).getLane(), processModel.getElem(exPI).getPool(), processToText.dataModel.process.EventType.START_EVENT);
            alternativePathModel.addEvent(startEvent);

            // Reallocate elems to alternative path
            buildAlternativePathModel(exPI, true, processModel, alternativePathModel, exPI);

            // Add arc from artifical start to real start elem
            processToText.dataModel.process.Event realStart = (processToText.dataModel.process.Event) alternativePathModel.getElem(exPI);
            alternativePathModel.addArc(new processToText.dataModel.process.Arc(generateModelId("newArc" + genericId), "", startEvent, realStart));

            // Add path to model
            processModel.addAlternativePath(alternativePathModel, exPI);

        }
    }

    // TODO: Fazer
    private void buildAlternativePathModel(int id, boolean isElem, ProcessModel model, ProcessModel alternative, int exPI) {
    /*
        if (isElem) {
            JSONElem elem = elems.get(id);
            if (elem.getArcs().size() > 0) {
                for (int arc : elem.getArcs()) {
                    buildAlternativePathModel(arc, false, model, alternative, exPI);
                    alternative.addElem(model.getElem(id));
                    elems.remove(id);
                    model.removeElem(id);
                    // System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
                }
            } else {
                alternative.addElem(model.getElem(id));
                elems.remove(id);
                model.removeElem(id);
                // System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
            }
        } else {
            buildAlternativePathModel(arcs.get(id).getTarget(), true, model, alternative, exPI);
            alternative.addArc(model.getArc(id));
            arcs.remove(id);
            model.removeArc(id);
            // System.out.println("Arc reallocated: " + id + " --> " + exPI);
        }
    */
    }

}