package br.edu.ufrgs.inf.bpm.builder.elementType;

import org.omg.spec.bpmn._20100524.model.TActivity;
import org.omg.spec.bpmn._20100524.model.TSubProcess;

public class ActivityType {

    private static final int noneType = 0;
    private static final int subProcessType = 3;

    public static int getActivityType(TActivity activity) throws IllegalArgumentException {
        if (activity instanceof TSubProcess) {
            return subProcessType;
        } else {
            return noneType;
        }
    }

}
