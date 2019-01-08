package br.edu.ufrgs.inf.bpm.builder.elementType;

import org.omg.spec.bpmn._20100524.model.TActivity;
import org.omg.spec.bpmn._20100524.model.TSubProcess;

public class ActivityType {

    private static final int noneType = 0;
    private static final int expandedSubProcessType = 3;

    public static int getActivityType(TActivity activity) throws IllegalArgumentException {
        if (activity instanceof TSubProcess) {
            TSubProcess subProcess = (TSubProcess) activity;
            if (subProcess.getFlowElement().size() > 0) {
                return expandedSubProcessType;
            }
        }
        return noneType;
    }

}
