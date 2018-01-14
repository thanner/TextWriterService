package br.edu.ufrgs.inf.bpm.bpmparser.communication;

public enum ElementType {
    STARTEVENT("StartEvent"),
    ENDEVENT("EndEvent"),
    ACTIVITY("Activity"),
    XORGATEWAYSPLIT("ExclusiveGateway"),
    XORGATEWAYJOIN("ExclusiveGateway"),
    ANDGATEWAYSPIT("ParallelGateway"),
    ANDGATEWAYJOIN("ParallelGateway"),
    POOL("Pool"),
    LANE("Lane"),
    FLOW("Flow");

    private final String name;

    ElementType(String string) {
        this.name = string;
    }

    public String toString() {
        return this.name;
    }
}
