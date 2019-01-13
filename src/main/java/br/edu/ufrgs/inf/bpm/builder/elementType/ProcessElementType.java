package br.edu.ufrgs.inf.bpm.builder.elementType;

public enum ProcessElementType {
    ACTIVITY("ACTIVITY"),
    STARTEVENT("STARTEVENT"), ENDEVENT("ENDEVENT"), INTERMEDIATEEVENT("INTERMEDIATEEVENT"),
    XORSPLIT("XORJOIN"), XORJOIN("XORJOIN"),
    ANDSPLIT("ANDSPLIT"), ANDJOIN("ANDJOIN"),
    ORSPLIT("ORSPLIT"), ORJOIN("ORJOIN"),
    GATEWAYBASEDEVENTSPLIT("GATEWAYBASEDEVENTSPLIT"), UNKNOWN("UNKNOWN");

    private final String processElementType;

    ProcessElementType(String processElementType) {
        this.processElementType = processElementType;
    }

    public String value() {
        return processElementType;
    }
}
