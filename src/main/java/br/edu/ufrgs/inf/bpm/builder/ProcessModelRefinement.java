package br.edu.ufrgs.inf.bpm.builder;

import processToText.dataModel.process.Activity;
import processToText.dataModel.process.ProcessModel;

import java.util.Map;

public class ProcessModelRefinement {

    private ProcessModel processModel;

    public ProcessModelRefinement(ProcessModel processModel) {
        this.processModel = processModel;
    }

    public ProcessModel refineProcessModel() {
        refineActivityLabel();
        return processModel;
    }

    private void refineActivityLabel() {
        for (Map.Entry<Integer, Activity> activityEntrySet : processModel.getActivites().entrySet()) {
            Activity activity = activityEntrySet.getValue();
            if (activity.getLabel().replaceAll("\n", "").isEmpty()) {
                activity.setLabel("Do activity with id " + activity.getId() + "\n");
            }
        }
    }

}
