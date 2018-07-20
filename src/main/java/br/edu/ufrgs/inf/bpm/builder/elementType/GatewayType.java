package br.edu.ufrgs.inf.bpm.builder.elementType;

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
