package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.builder.elementType.ActivityType;
import br.edu.ufrgs.inf.bpm.builder.elementType.EventType;
import br.edu.ufrgs.inf.bpm.builder.elementType.GatewayType;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import org.omg.spec.bpmn._20100524.model.*;
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

    private BpmnWrapper bpmnWrapper;
    //private Map<Integer, Integer> externalPathInitiators;

    public ProcessModelBuilder() {
        genericId = 0;
        bpmnIdMap = new HashMap<>();

        laneMap = new HashMap<>();
        poolMap = new HashMap<>();
        elementMap = new HashMap<>();
        tArcMap = new HashMap<>();
        arcMap = new HashMap<>();

        //externalPathInitiators = new HashMap<>();
    }

    public ProcessModel buildProcess(TDefinitions definitions) {
        bpmnWrapper = new BpmnWrapper(definitions);

        int newId = generateModelId("ProcessModel1");
        ProcessModel processModel = new ProcessModel(newId, "Process Model");

        List<TProcess> processList = bpmnWrapper.getProcessList();

        for (TProcess process : processList) {

            processModel.addPool(createPool(process));
            for (TLaneSet tLaneSet : process.getLaneSet()) {
                getLanes(tLaneSet, processModel, process);
            }

            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TActivity) {
                    processModel.addActivity(createActivity((TActivity) flowElement.getValue(), processModel, null));
                } else if (flowElement.getValue() instanceof TEvent) {
                    processModel.addEvent(createEvent((TEvent) flowElement.getValue(), null));
                } else if (flowElement.getValue() instanceof TGateway) {
                    processModel.addGateway(createGateway((TGateway) flowElement.getValue(), null));
                }
            }

            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TSequenceFlow) {
                    processModel.addArc(createArc((TSequenceFlow) flowElement.getValue()));
                }
            }

            /*
            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (flowElement.getValue() instanceof TBoundaryEvent) {
                    attachEvent(process, processModel, (TBoundaryEvent) flowElement.getValue());
                }
            }

            removeExternalPathInitiators(process, processModel);
            */
        }

        connectSubprocess(processModel);

        return processModel;
    }

    private String createPool(TProcess process) {
        int newId = generateModelId(process.getId());
        Pool modelPool = new Pool(newId, bpmnWrapper.getProcessName(process));
        poolMap.put(process.getId(), modelPool);
        return modelPool.getName();
    }

    private void getLanes(TLaneSet tLaneSet, ProcessModel processModel, TProcess process) {
        if (tLaneSet != null) {
            for (TLane lane : tLaneSet.getLane()) {
                processModel.addLane(createLane(lane, process));
                getLanes(lane.getChildLaneSet(), processModel, process);
            }
        }
    }

    private String createLane(TLane lane, TProcess process) {
        int newId = generateModelId(lane.getId());
        Lane modelLane = new Lane(newId, getName(lane.getName()), poolMap.get(process.getId()));
        laneMap.put(lane.getId(), modelLane);
        return modelLane.getName();
    }

    private Activity createActivity(TActivity activity, ProcessModel processModel, TSubProcess tSubProcess) {
        try {
            int newId = generateModelId(activity.getId());
            int activityType = getActivityType(activity);
            Activity modelActivity = new Activity(newId, getName(activity.getName()), getLaneByObject(activity), getPoolByObject(activity), activityType);
            elementMap.put(activity.getId(), modelActivity);

            if (tSubProcess != null) {
                modelActivity.setSubProcessID(elementMap.get(tSubProcess.getId()).getId());
            }

            if (activity instanceof TSubProcess) {
                createSubProcessElements((TSubProcess) activity, processModel);
            }

            return modelActivity;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private void createSubProcessElements(TSubProcess tSubProcess, ProcessModel processModel) {
        for (JAXBElement<? extends TFlowElement> flowElement : tSubProcess.getFlowElement()) {
            if (flowElement.getValue() instanceof TActivity) {
                processModel.addActivity(createActivity((TActivity) flowElement.getValue(), processModel, tSubProcess));
            } else if (flowElement.getValue() instanceof TEvent) {
                processModel.addEvent(createEvent((TEvent) flowElement.getValue(), tSubProcess));
            } else if (flowElement.getValue() instanceof TGateway) {
                processModel.addGateway(createGateway((TGateway) flowElement.getValue(), tSubProcess));
            }
        }

        for (JAXBElement<? extends TFlowElement> flowElement : tSubProcess.getFlowElement()) {
            if (flowElement.getValue() instanceof TSequenceFlow) {
                processModel.addArc(createArc((TSequenceFlow) flowElement.getValue()));
            }
        }

        /*
        for (JAXBElement<? extends TFlowElement> flowElement : tSubProcess.getFlowElement()) {
            if (flowElement.getValue() instanceof TBoundaryEvent) {
                attachEvent(bpmnWrapper.getProcessByFlowElement(tSubProcess), processModel, (TBoundaryEvent) flowElement.getValue());
            }
        }
        */
    }

    private Event createEvent(TEvent event, TSubProcess tSubProcess) {
        try {
            int newId = generateModelId(event.getId());
            int eventType = getEventType(event);
            Event modelEvent = new Event(newId, getName(event.getName()), getLaneByObject(event), getPoolByObject(event), eventType);
            elementMap.put(event.getId(), modelEvent);
            if (tSubProcess != null) {
                modelEvent.setSubProcessID(elementMap.get(tSubProcess.getId()).getId());
            }
            return modelEvent;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Gateway createGateway(TGateway gateway, TSubProcess tSubProcess) {
        try {
            int newId = generateModelId(gateway.getId());
            int gatewayType = getGatewayType(gateway);
            Gateway modelGateway = new Gateway(newId, getName(gateway.getName()), getLaneByObject(gateway), getPoolByObject(gateway), gatewayType);
            elementMap.put(gateway.getId(), modelGateway);
            if (tSubProcess != null) {
                modelGateway.setSubProcessID(elementMap.get(tSubProcess.getId()).getId());
            }
            return modelGateway;
        } catch (IllegalArgumentException i) {
            i.printStackTrace();
        }
        return null;
    }

    private Arc createArc(TSequenceFlow arc) {
        int newId = generateModelId(arc.getId());
        Arc modelArc = new Arc(newId, getName(arc.getName()), elementMap.get(((TFlowNode) arc.getSourceRef()).getId()), elementMap.get(((TFlowNode) arc.getTargetRef()).getId()), "SequenceFlow");
        arcMap.put(newId, modelArc);
        tArcMap.put(arc.getId(), modelArc);
        return modelArc;
    }

    /*
    private void attachEvent(TProcess process, ProcessModel processModel, TBoundaryEvent tBoundaryEvent) {
        String tActivityId = tBoundaryEvent.getAttachedToRef().getLocalPart();
        Activity activityWithAttach = (Activity) elementMap.get(tActivityId);
        Event boundaryEvent = (Event) elementMap.get(tBoundaryEvent.getId());

        activityWithAttach.addAttachedEvent(boundaryEvent.getId());
        boundaryEvent.setAttached(true);
        boundaryEvent.setIsAttachedTo(activityWithAttach.getId());

        TActivity tActivityWithAttach = bpmnWrapper.getTActivityAttached(process, tActivityId);
        if (tActivityWithAttach != null) {
            // Attached event leads to alternative path
            //if (tBoundaryEvent.getOutgoing().size() > 0) {
            // externalPathInitiators.put(boundaryEvent.getId(), activityWithAttach.getId());
            // Attached event goes back to standard path
            //} else {
            Arc arc = new Arc(generateModelId("newArc" + genericId), "", activityWithAttach, boundaryEvent, "VirtualFlow");
            processModel.addArc(arc);
            //}
        }
    }
    */

    private String getName(String name) {
        return name != null ? name : "";
    }

    private int getActivityType(TActivity activity) throws IllegalArgumentException {
        try {
            return ActivityType.getActivityType(activity);
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(activity);
        }
    }

    private int getEventType(TEvent event) throws IllegalArgumentException {
        try {
            return EventType.getEventType(event);
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(event);
        }
    }

    private int getGatewayType(TGateway gateway) throws IllegalArgumentException {
        try {
            return GatewayType.getGatewayType(gateway);
        } catch (IllegalArgumentException e) {
            throw getIllegalTypeException(gateway);
        }
    }

    private IllegalArgumentException getIllegalTypeException(TFlowNode flowNode) {
        return new IllegalArgumentException("Can not find element type (Element: " + flowNode.getClass().getSimpleName() + ". Id: " + flowNode.getId() + ")");
    }

    private Pool getPoolByObject(TFlowNode flowNode) {
        TProcess process = bpmnWrapper.getProcessByFlowElement(flowNode);
        return process != null ? poolMap.get(process.getId()) : null;
    }

    private Lane getLaneByObject(TFlowNode flowNode) {
        TLane lane = bpmnWrapper.getInternalLaneByFlowElement(flowNode);
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

    /*
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

            externalPathInitiators.remove(exPI);
        }
    }
    */

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
                        model.removeElem(id);
                        //System.out.println("Element 1 reallocated: " + id + " " + element.getLabel() + " --> " + exPI);
                    }
                }
            } else {
                Element element = model.getElem(id);
                if (element != null) {
                    alternative.addElem(element);
                    model.removeElem(id);
                    //System.out.println("Element 2 reallocated: " + id + " " + element.getLabel() + " --> " + exPI);
                }
            }
        } else {
            buildAlternativePathModel(tProcess, arcMap.get(id).getTarget().getId(), true, model, alternative, exPI);

            Arc arc = model.getArc(id);
            if (arc != null) {
                alternative.addArc(arc);
                model.removeArc(id);
                //System.out.println("Arc reallocated: " + id + " --> " + exPI);
            }
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

    public void connectSubprocess(ProcessModel model) {
        // Connect inner of subproess to process model
        for (processToText.dataModel.process.Activity a : model.getActivites().values()) {
            if (a.getType() == processToText.dataModel.process.ActivityType.SUBPROCESS) {
                int subProcesID = a.getId();
                processToText.dataModel.process.Element out = null;
                int removeout = -1;

                // Remove arcs from subprocess activity
                for (processToText.dataModel.process.Arc arc : model.getArcs().values()) {
                    if (arc.getSource() == a) {
                        out = arc.getTarget();
                        removeout = arc.getId();
                    }
                }
                model.removeArc(removeout);

                // Check all activities belonging to subprocess
                for (processToText.dataModel.process.Event subE : model.getEvents().values()) {
                    if (subE.getSubProcessID() == subProcesID) {
                        boolean hasInput = false;
                        boolean hasOutput = false;
                        for (processToText.dataModel.process.Arc arc : model.getArcs().values()) {
                            if (arc.getSource() == subE) {
                                hasOutput = true;
                            }
                            if (arc.getTarget() == subE) {
                                hasInput = true;
                            }
                        }
                        if (!hasInput) {
                            model.addArc(new Arc(generateModelId("newArc" + genericId), "", a, subE, "SequenceFlow"));
                        }
                        if (!hasOutput) {
                            model.addArc(new Arc(generateModelId("newArc" + genericId), "", subE, out, "SequenceFlow"));
                        }
                    }
                }
            }
        }
    }

}