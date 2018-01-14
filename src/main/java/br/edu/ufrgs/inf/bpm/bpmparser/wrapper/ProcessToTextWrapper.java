package br.edu.ufrgs.inf.bpm.bpmparser.wrapper;

import org.semanticweb.owlapi.model.OWLOntology;
import processToText.Main;
import processToText.dataModel.jsonIntermediate.*;
import processToText.dataModel.process.*;

import java.util.HashMap;

public class ProcessToTextWrapper {

    private static HashMap<String, Integer> keyMap;
    private static HashMap<Integer, JSONArc> arcs;
    private static HashMap<Integer, JSONElem> elems;
    private static int idCounter;


    public static String convertToText(OWLOntology ontology) {
        ProcessModel model = getProcessModel(ontology);

        int counter = 0;
        String surfaceText = "";

        try {
            // Multi Pool Model
            if (model.getPools().size() > 1) {
                long time = System.currentTimeMillis();
                System.out.println();
                System.out.print("The model contains " + model.getPools().size() + " pools: ");
                int count = 0;
                for (String role : model.getPools()) {
                    if (count > 0 && model.getPools().size() > 2) {
                        System.out.print(", ");
                    }
                    if (count == model.getPools().size() - 1) {
                        System.out.print(" and ");
                    }
                    System.out.print(role + " (" + (count + 1) + ")");
                    count++;
                }

                HashMap<Integer, ProcessModel> newModels = model.getModelForEachPool();
                for (ProcessModel m : newModels.values()) {
                    try {
                        m.normalize();
                        m.normalizeEndEvents();
                    } catch (Exception e) {
                        System.out.println("Error: Normalization impossible");
                        e.printStackTrace();
                    }
                    surfaceText = Main.toText(m, counter);
                    System.out.println(surfaceText.replaceAll(" process ", " " + m.getPools().get(0) + " process "));
                }
            } else {
                try {
                    model.normalize();
                    model.normalizeEndEvents();
                } catch (Exception e) {
                    System.out.println("Error: Normalization impossible");
                    e.printStackTrace();
                }
                surfaceText = Main.toText(model, counter);
                System.out.println(surfaceText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return surfaceText;
    }

    private static ProcessModel getProcessModel(OWLOntology ontology) {
        ProcessModel model = new ProcessModel(-1, "Process Model");
        HashMap<Integer, processToText.dataModel.process.Element> idMap = new HashMap<>();
        idMap = new HashMap<>();
        HashMap<Integer, processToText.dataModel.process.Lane> laneMap = new HashMap<>();
        HashMap<Integer, processToText.dataModel.process.Pool> poolMap = new HashMap<>();

        mapPoolsAndLanes(model, laneMap, poolMap);
        mapProcessElements(model, laneMap, poolMap, idMap);
        mapArcs(model, idMap);
        mapSubprocess(model);

        return model;
    }

    private static void mapPoolsAndLanes(ProcessModel model, HashMap<Integer, Lane> laneMap, HashMap<Integer, Pool> poolMap) {
        // Map Pools
        for (JSONPool jPool : OntologyOutput.getPools()) {
            processToText.dataModel.process.Pool pool = new processToText.dataModel.process.Pool(jPool.getId(), jPool.getLabel());
            model.addPool(jPool.getLabel());
            poolMap.put(jPool.getId(), pool);

            // Map Lanes
            for (JSONLane jLane : OntologyOutput.getLanes()) {
                processToText.dataModel.process.Lane lane = new processToText.dataModel.process.Lane(jLane.getId(), jLane.getLabel(), pool);
                model.addLane(jLane.getLabel());
                laneMap.put(jLane.getId(), lane);
            }
        }
    }

    private static void mapProcessElements(ProcessModel model, HashMap<Integer, Lane> laneMap, HashMap<Integer, Pool> poolMap, HashMap<Integer, Element> idMap) {
        // Iterate over all elems to create the according model objects
        for (JSONTask activityElement : OntologyOutput.getActivities()) {
            //processToText.dataModel.jsonIntermediate.JSONTask jTask = (processToText.dataModel.jsonIntermediate.JSONTask) elem;
            processToText.dataModel.process.Activity activity = new processToText.dataModel.process.Activity(activityElement.getId(), activityElement.getLabel().replaceAll("\n", " "), laneMap.get(activityElement.getLaneId()), poolMap.get(activityElement.getPoolId()), processToText.dataModel.process.ActivityType.TYPE_MAP.get(activityElement.getType()));
            if (activityElement.getSubProcessID() > 0) {
                activity.setSubProcessID(activityElement.getSubProcessID());
            }
            model.addActivity(activity);
            idMap.put(activityElement.getId(), activity);
        }

        for (JSONEvent eventElement : OntologyOutput.getEvents()) {
            // processToText.dataModel.jsonIntermediate.JSONEvent jEvent = (processToText.dataModel.jsonIntermediate.JSONEvent) elem;
            processToText.dataModel.process.Event event = new processToText.dataModel.process.Event(eventElement.getId(), eventElement.getLabel(), laneMap.get(eventElement.getLaneId()), poolMap.get(eventElement.getPoolId()), getEventType(eventElement));
            if (eventElement.getSubProcessID() > 0) {
                event.setSubProcessID(eventElement.getSubProcessID());
            }
            model.addEvent(event);
            idMap.put(eventElement.getId(), event);
        }

        for (JSONGateway gatewayElement : OntologyOutput.getGateways()) {
            // processToText.dataModel.jsonIntermediate.JSONGateway jGateway = (processToText.dataModel.jsonIntermediate.JSONGateway) elem;
            processToText.dataModel.process.Gateway gateway = new processToText.dataModel.process.Gateway(gatewayElement.getId(), gatewayElement.getLabel(), laneMap.get(gatewayElement.getLaneId()), poolMap.get(gatewayElement.getPoolId()), GatewayType.TYPE_MAP.get(gatewayElement.getType()));
            if (gatewayElement.getSubProcessID() > 0) {
                gateway.setSubProcessID(gatewayElement.getSubProcessID());
            }
            model.addGateway(gateway);
            idMap.put(gatewayElement.getId(), gateway);
        }
    }

    // TODO: Fazer: Ver bookmark "Método que cria"
    private static void mapArcs(ProcessModel model, HashMap<Integer, Element> idMap) {
        HashMap<Integer, Integer> externalPathInitiators = new HashMap<Integer, Integer>();

        // Iterate over all elems to create the according arcs
        for (JSONElem elem : OntologyOutput.getElements()) {
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

        // remove all external path initiators
        for (int exPI : externalPathInitiators.keySet()) {
            ProcessModel alternativePathModel = new ProcessModel(exPI, "");

            // Create start event
            processToText.dataModel.process.Event startEvent = new processToText.dataModel.process.Event(getId(), "", model.getElem(exPI).getLane(), model.getElem(exPI).getPool(), processToText.dataModel.process.EventType.START_EVENT);
            alternativePathModel.addEvent(startEvent);

            // Reallocate elems to alternative path
            buildAlternativePathModel(exPI, true, model, alternativePathModel, exPI);

            // Add arc from artifical start to real start elem
            processToText.dataModel.process.Event realStart = (processToText.dataModel.process.Event) alternativePathModel.getElem(exPI);
            alternativePathModel.addArc(new processToText.dataModel.process.Arc(getId(), "", startEvent, realStart));

            // Add path to model
            model.addAlternativePath(alternativePathModel, exPI);

        }
    }

    // TODO: Fazer: Ver bookmark "Método que cria"
    private static void mapSubprocess(ProcessModel model) {
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
                            model.addArc(new processToText.dataModel.process.Arc(getId(), "", a, subE, "SequenceFlow"));
                        }
                        if (!hasOutput) {
                            model.addArc(new processToText.dataModel.process.Arc(getId(), "", subE, out, "SequenceFlow"));
                        }
                    }
                }
            }
        }
    }

    private static int getEventType(JSONEvent eventElement) {
        try {
            int type = processToText.dataModel.process.EventType.TYPE_MAP.get(eventElement.getType());
            return type;
        } catch (Exception e) {
            System.out.println("Error: Event Mapping (" + eventElement.getType() + ")");
        }
        return 5;
    }

    private static void buildAlternativePathModel(int id, boolean isElem, ProcessModel model, ProcessModel alternative, int exPI) {
        if (isElem) {
            JSONElem elem = elems.get(id);
            if (elem.getArcs().size() > 0) {
                for (int arc : elem.getArcs()) {
                    buildAlternativePathModel(arc, false, model, alternative, exPI);
                    alternative.addElem(model.getElem(id));
                    elems.remove(id);
                    model.removeElem(id);
                    System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
                }
            } else {
                alternative.addElem(model.getElem(id));
                elems.remove(id);
                model.removeElem(id);
                System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
            }
        } else {
            buildAlternativePathModel(arcs.get(id).getTarget(), true, model, alternative, exPI);
            alternative.addArc(model.getArc(id));
            arcs.remove(id);
            model.removeArc(id);
            System.out.println("Arc reallocated: " + id + " --> " + exPI);
        }
    }

    private static int getId() {
        int id = idCounter;
        idCounter++;
        keyMap.put("newElem" + id, id);
        return id;
    }

}
