package br.edu.ufrgs.inf.bpm.wrapper.elementType;

public enum EventType {
    TStartEvent(1), TEndEvent(11);

    private final int id;

    EventType(int id) {this.id = id;}

    public int getValue() { return id;}


    // public static final int START_EVENT = 1;
    // public static final int START_MSG = 2;
    // public static final int START_TIMER = 3;

    // public static final int END_EVENT = 11;
    // public static final int END_ERROR = 12;
    // public static final int END_SIGNAL = 13;
    // public static final int END_MSG = 14;

    // public static final int INTM = 21;
    // public static final int INTM_TIMER = 22;
    // public static final int INTM_CANCEL = 23;
    // public static final int INTM_CONDITIONAL = 24;
    // public static final int INTM_ESCALATION = 25;
    // public static final int INTM_ERROR = 26;

    // public static final int INTM_ESCALATION_THR = 31;
    // public static final int INTM_SIGNAL_THR = 32;
    // public static final int INTM_MULTIPLE_THR = 33;
    // public static final int INTM_LINK_THR = 34;
    // public static final int INTM_MSG_THR = 35;

    // public static final int INTM_ESCALATION_CAT = 41;
    // public static final int INTM_MULTIPLE_CAT = 42;
    // public static final int INTM_LINK_CAT = 43;
    // public static final int INTM_MSG_CAT = 44;
    // public static final int INTM_PMULT_CAT = 45;
    // public static final int INTM_COMPENSATION_CAT = 46;
}
