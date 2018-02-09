package br.edu.ufrgs.inf.bpm.wrapper.elementType;

public enum ActivityType {
    TTask(0), TSubProcess(3);

    private final int id;

    ActivityType(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
