package br.edu.ufrgs.inf.bpm.wrapper.elementType;

public enum GatewayType {
    TExclusiveGateway(0), TInclusiveGateway(1), TParallelGateway(2), TEventBasedGateway(3);

    private final int id;

    GatewayType(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
