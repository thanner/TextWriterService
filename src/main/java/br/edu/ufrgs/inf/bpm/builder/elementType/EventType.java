package br.edu.ufrgs.inf.bpm.builder.elementType;

import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;

public class EventType {

    public static final int START_EVENT = 1;
    public static final int START_MSG = 2;
    public static final int START_TIMER = 3;

    public static final int END_EVENT = 11;
    public static final int END_ERROR = 12;
    public static final int END_SIGNAL = 13;
    public static final int END_MSG = 14;

    public static final int INTM = 21;
    public static final int INTM_TIMER = 22;
    public static final int INTM_CANCEL = 23;
    public static final int INTM_CONDITIONAL = 24;
    public static final int INTM_ESCALATION = 25;
    public static final int INTM_ERROR = 26;

    public static final int INTM_ESCALATION_THR = 31;
    public static final int INTM_SIGNAL_THR = 32;
    public static final int INTM_MULTIPLE_THR = 33;
    public static final int INTM_LINK_THR = 34;
    public static final int INTM_MSG_THR = 35;

    public static final int INTM_ESCALATION_CAT = 41;
    public static final int INTM_MULTIPLE_CAT = 42;
    public static final int INTM_LINK_CAT = 43;
    public static final int INTM_MSG_CAT = 44;
    public static final int INTM_PMULT_CAT = 45;
    public static final int INTM_COMPENSATION_CAT = 46;


    public static int getEventType(TEvent event) throws IllegalArgumentException {
        if (event instanceof TStartEvent) {
            TStartEvent startEvent = (TStartEvent) event;
            return getStartEvent(startEvent);
        } else if (event instanceof TIntermediateThrowEvent) {
            TIntermediateThrowEvent intermediateThrowEvent = (TIntermediateThrowEvent) event;
            return getIntermediateThrowEvent(intermediateThrowEvent);
        } else if (event instanceof TIntermediateCatchEvent) {
            TIntermediateCatchEvent intermediateCatchEvent = (TIntermediateCatchEvent) event;
            return getIntermediateCatchEvent(intermediateCatchEvent);
        } else if (event instanceof TBoundaryEvent) {
            TBoundaryEvent boundaryEvent = (TBoundaryEvent) event;
            return getBoundaryEvent(boundaryEvent);
        } else if (event instanceof TEndEvent) {
            TEndEvent endEvent = (TEndEvent) event;
            return getEndEvent(endEvent);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static int getStartEvent(TStartEvent startEvent) {
        for (JAXBElement<? extends TEventDefinition> eventDefinition : startEvent.getEventDefinition()) {
            if (eventDefinition.getValue() instanceof TMessageEventDefinition) {
                return START_MSG;
            } else if (eventDefinition.getValue() instanceof TTimerEventDefinition) {
                return START_TIMER;
            }
        }
        return START_EVENT;
    }

    private static int getIntermediateThrowEvent(TIntermediateThrowEvent intermediateThrowEvent) {
        for (JAXBElement<? extends TEventDefinition> jaxbEventDefinition : intermediateThrowEvent.getEventDefinition()) {
            TEventDefinition eventDefinition = jaxbEventDefinition.getValue();
            if (eventDefinition instanceof TEscalationEventDefinition) {
                return INTM_ESCALATION_THR;
            } else if (eventDefinition instanceof TSignalEventDefinition) {
                return INTM_SIGNAL_THR;
            } else if (eventDefinition instanceof TLinkEventDefinition) {
                return INTM_LINK_THR;
            } else if (eventDefinition instanceof TMessageEventDefinition) {
                return INTM_MSG_THR;
            } else {
                int element = getIntermediate(eventDefinition);
                if (element != INTM) {
                    return element;
                }
            }
        }
        return INTM;
    }

    private static int getIntermediateCatchEvent(TIntermediateCatchEvent intermediateCatchEvent) {
        for (JAXBElement<? extends TEventDefinition> jaxbEventDefinition : intermediateCatchEvent.getEventDefinition()) {
            TEventDefinition eventDefinition = jaxbEventDefinition.getValue();
            if (eventDefinition instanceof TEscalationEventDefinition) {
                return INTM_ESCALATION_CAT;
            } else if (eventDefinition instanceof TLinkEventDefinition) {
                return INTM_LINK_CAT;
            } else if (eventDefinition instanceof TMessageEventDefinition) {
                return INTM_MSG_CAT;
            } else if (eventDefinition instanceof TCompensateEventDefinition) {
                return INTM_COMPENSATION_CAT;
            } else {
                int element = getIntermediate(eventDefinition);
                if (element != INTM) {
                    return element;
                }
            }
        }
        return INTM;
    }

    private static int getIntermediate(TEventDefinition eventDefinition) {
        if (eventDefinition instanceof TTimerEventDefinition) {
            return INTM_TIMER;
        } else if (eventDefinition instanceof TCancelEventDefinition) {
            return INTM_CANCEL;
        } else if (eventDefinition instanceof TConditionalEventDefinition) {
            return INTM_CONDITIONAL;
        } else if (eventDefinition instanceof TEscalationEventDefinition) {
            return INTM_ESCALATION;
        } else if (eventDefinition instanceof TErrorEventDefinition) {
            return INTM_ERROR;
        }

        return INTM;
    }

    private static int getBoundaryEvent(TBoundaryEvent boundaryEvent) {
        for (JAXBElement<? extends TEventDefinition> eventDefinition : boundaryEvent.getEventDefinition()) {
            if (eventDefinition.getValue() instanceof TEscalationEventDefinition) {
                return INTM_ESCALATION_THR;
            } else if (eventDefinition.getValue() instanceof TSignalEventDefinition) {
                return INTM_SIGNAL_THR;
            } else if (eventDefinition.getValue() instanceof TLinkEventDefinition) {
                return INTM_LINK_THR;
            } else if (eventDefinition.getValue() instanceof TMessageEventDefinition) {
                return INTM_MSG_THR;
            } else if (eventDefinition.getValue() instanceof TCompensateEventDefinition) {
                return INTM_COMPENSATION_CAT;
            }
        }
        return INTM;
    }

    private static int getEndEvent(TEndEvent endEvent) {
        for (JAXBElement<? extends TEventDefinition> eventDefinition : endEvent.getEventDefinition()) {
            if (eventDefinition.getValue() instanceof TErrorEventDefinition) {
                return END_ERROR;
            } else if (eventDefinition.getValue() instanceof TSignalEventDefinition) {
                return END_SIGNAL;
            } else if (eventDefinition.getValue() instanceof TMessageEventDefinition) {
                return END_MSG;
            }
        }
        return END_EVENT;
    }

}
