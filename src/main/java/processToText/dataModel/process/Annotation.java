package processToText.dataModel.process;

import java.util.ArrayList;

public class Annotation {

    private ArrayList<String> actions;
    private ArrayList<String> businessObjects;
    private String addition;

    public Annotation() {
        actions = new ArrayList<String>();
        businessObjects = new ArrayList<String>();
        addition = "";
    }

    public void addAction(String action) {
        actions.add(action);
    }

    public void addBusinessObjects(String bo) {
        businessObjects.add(bo);
    }

    public ArrayList<String> getActions() {
        return actions;
    }

    public ArrayList<String> getBusinessObjects() {
        return businessObjects;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String add) {
        addition = add;
    }

    public String toString() {
        String s = actions.toString() + " " + businessObjects.toString() + " " + addition;
        return s;

    }


}
