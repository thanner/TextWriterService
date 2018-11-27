package br.edu.ufrgs.inf.bpm.builder.elementType;

import org.omg.spec.bpmn._20100524.model.*;

public class GatewayType {

    private static final int exclusiveGatewayType = 0;
    private static final int inclusiveGatewayType = 1;
    private static final int parallelGatewayType = 2;
    private static final int eventBasedGatewayType = 3;

    public static int getGatewayType(TGateway gateway) throws IllegalArgumentException {
        if (gateway instanceof TExclusiveGateway) {
            return exclusiveGatewayType;
        } else if (gateway instanceof TInclusiveGateway) {
            return inclusiveGatewayType;
        } else if (gateway instanceof TParallelGateway) {
            return parallelGatewayType;
        } else if (gateway instanceof TEventBasedGateway) {
            return eventBasedGatewayType;
        } else {
            throw new IllegalArgumentException();
        }
    }

}
