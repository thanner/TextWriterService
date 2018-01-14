package br.edu.ufrgs.inf.bpm.wrapper;/*
package br.edu.ufrgs.inf.bpm.wrapper;

import processToText.dataModel.process.*;

public class ProcessModelWrapper {

    public void addPool(ProcessModel processModel, String pool) {
        // process.addPool(new Pool(Integer.parseInt(object.getId()), object.getText()));
        processModel.addPool(pool);
    }

    public void addLane(ProcessModel processModel, String lane) {
        // process.addLane(new Lane(Integer.parseInt(object.getId()), object.getText(), getPoolByObject(object)));
        processModel.addLane(lane);
    }

    public void addActivity(ProcessModel processModel, br.edu.ufrgs.inf.bpm.process.Activity activity) {
        processModel.addActivity(getActivity(activity));
    }

    public void addEvent(ProcessModel processModel, br.edu.ufrgs.inf.bpm.process.Event event) {
        processModel.addEvent(getEvent(event));
    }

    public void addGateway(ProcessModel processModel, br.edu.ufrgs.inf.bpm.process.Gateway gateway) {
        processModel.addGateway(getGateway(gateway));
    }

    public void addArc(ProcessModel processModel, br.edu.ufrgs.inf.bpm.process.Arc arc) {
        processModel.addArc(getArc(arc));
    }

    private Lane getLaneByObject(br.edu.ufrgs.inf.bpm.process.Element object) {
        return new Lane(object.getLane().getId(), object.getLane().getName(), getPoolByObject(object));
    }

    private Pool getPoolByObject(br.edu.ufrgs.inf.bpm.process.Element object) {
        return new Pool(object.getId(), object.getLabel());
    }

    private Element getElement(br.edu.ufrgs.inf.bpm.process.Element object) {
        Element element = null;

        if (object instanceof br.edu.ufrgs.inf.bpm.process.Activity) {
            element = getActivity((br.edu.ufrgs.inf.bpm.process.Activity) object);
        } else if (object instanceof br.edu.ufrgs.inf.bpm.process.Event) {
            element = getEvent((br.edu.ufrgs.inf.bpm.process.Event) object);
        } else if (object instanceof br.edu.ufrgs.inf.bpm.process.Gateway) {
            element = getGateway((br.edu.ufrgs.inf.bpm.process.Gateway) object);
        }

        return element;
    }

    private Activity getActivity(br.edu.ufrgs.inf.bpm.process.Activity activity) {
        return new Activity(activity.getId(), activity.getLabel(), getLaneByObject(activity), getPoolByObject(activity), activity.getType());
    }

    private Event getEvent(br.edu.ufrgs.inf.bpm.process.Event event) {
        return new Event(event.getId(), event.getLabel(), getLaneByObject(event), getPoolByObject(event), event.getType());
    }

    private Gateway getGateway(br.edu.ufrgs.inf.bpm.process.Gateway gateway) {
        return new Gateway(gateway.getId(), gateway.getLabel(), getLaneByObject(gateway), getPoolByObject(gateway), gateway.getType());
    }

    private Arc getArc(br.edu.ufrgs.inf.bpm.process.Arc arc) {
        return new Arc(arc.getId(), arc.getLabel(), getElement(arc.getSource()), getElement(arc.getTarget()), arc.getType());
    }

}
*/