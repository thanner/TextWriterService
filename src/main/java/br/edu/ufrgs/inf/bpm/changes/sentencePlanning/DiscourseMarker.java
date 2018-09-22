package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.metatext.ProcessElementType;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.omg.spec.bpmn._20100524.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.textPlanning.IntermediateToDSynTConverter;

import java.util.*;

public class DiscourseMarker {

    private Map<Integer, Integer> levelOrdinalMap = new HashMap<>();
    private Set<String> parallelGatewayIndexSet = new HashSet<>();
    private BpmnWrapper bpmnWrapper;

    private int lastLevel;
    private int currentLevel;

    private int indexSequentialConnectors = 0;
    private int indexParallelConnectors = 0;

    public ArrayList<DSynTSentence> insertConnectives(ArrayList<DSynTSentence> textPlan, TDefinitions tDefinitions) {
        bpmnWrapper = new BpmnWrapper(tDefinitions);
        lastLevel = -1;

        for (int index = 0; index < textPlan.size(); index++) {
            DSynTSentence dSynTSentence = textPlan.get(index);
            currentLevel = dSynTSentence.getExecutableFragment().sen_level;

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.MAIN)) {
                DSynTMainSentence dSynTMainSentence = (DSynTMainSentence) dSynTSentence;
                if (!dSynTMainSentence.getExecutableFragment().sen_hasConnective && index > 0) {
                    insertConnectives(dSynTSentence, index, textPlan.size());
                }
                lastLevel = currentLevel;
            }

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.CONDITION)) {
                DSynTConditionSentence dSynTConditionSentence = (DSynTConditionSentence) dSynTSentence;
                if (!dSynTConditionSentence.getExecutableFragment().sen_hasConnective && index > 0 && !dSynTConditionSentence.getConditionFragment().sen_headPosition) {
                    //insertSequentialConnective(dSynTConditionSentence, index, textPlan.size());
                    insertConnectives(dSynTSentence, index, textPlan.size());
                }
                lastLevel = currentLevel;
            }

        }

        return textPlan;
    }


    private void insertConnectives(DSynTSentence dSynTSentence, int index, int textPlanSize) {
        List<ProcessElementDocument> processElementDocumentList = dSynTSentence.getProcessElementDocumentList();

        if (isAddExclusiveJoinDiscourseMarker(dSynTSentence)) {
            insertConnective(dSynTSentence, Lexemes.XOR_JOIN_CONNECTIVE);
        } else if (isAddInclusiveJoinDiscourseMarker(dSynTSentence)) {
            insertConnective(dSynTSentence, Lexemes.OR_JOIN_CONNECTIVE);
        } else if (isAddParallelJoinDiscourseMarker(dSynTSentence)) {
            insertConnective(dSynTSentence, Lexemes.AND_JOIN_CONNECTIVE);
        } else {
            if (dSynTSentence.getExecutableFragment().sen_hasBullet) {
                // Is a lateral sequence
                if (isAddParallelDiscourseMarker(dSynTSentence)) {
                    // Source is a parallel gateway (in the meantime, at the same time)
                    insertParallelConnective(dSynTSentence);
                } else if (isAddExclusiveInclusiveDiscourseMarker(dSynTSentence)) {
                    // Source is a exclusive/inclusive gateway (first, second, third)
                    adjustOrdinalIndex(currentLevel);
                    insertOrdinalConnective(dSynTSentence, currentLevel);
                }
            } else {
                if (isAddFinalDiscourseMarker(index, textPlanSize)) {
                    insertConnective(dSynTSentence, Lexemes.SEQUENCE_FINAL_CONNECTIVE);
                } else {
                    insertSequentialConnective(dSynTSentence);
                }
            }
        }
    }

    private boolean isAddParallelDiscourseMarker(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (tFlowElementSource instanceof TParallelGateway) {
                        String parallelGatewayIndex = tFlowElementSource.getId();
                        // Se esse gateway paralelo já apareceu alguma vez em outro caminho
                        if (parallelGatewayIndexSet.contains(parallelGatewayIndex)) {
                            return true;
                        } else {
                            parallelGatewayIndexSet.add(parallelGatewayIndex);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
        // return isAddDiscourseMarker(dSynTSentence, ProcessElementType.ANDSPLIT);
    }

    private boolean isAddExclusiveInclusiveDiscourseMarker(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (tFlowElementSource instanceof TExclusiveGateway || tFlowElementSource instanceof TInclusiveGateway) {
                        return true;
                    }
                }
            }
        }
        return false;
        // return isAddDiscourseMarker(dSynTSentence, ProcessElementType.XORSPLIT) || isAddDiscourseMarker(dSynTSentence, ProcessElementType.ORSPLIT);
    }

    private boolean isAddExclusiveJoinDiscourseMarker(DSynTSentence dSynTSentence) {
        return isAddDiscourseMarker(dSynTSentence, ProcessElementType.XORJOIN, TExclusiveGateway.class);
    }

    private boolean isAddInclusiveJoinDiscourseMarker(DSynTSentence dSynTSentence) {
        return isAddDiscourseMarker(dSynTSentence, ProcessElementType.ORJOIN, TInclusiveGateway.class);
    }

    private boolean isAddParallelJoinDiscourseMarker(DSynTSentence dSynTSentence) {
        return isAddDiscourseMarker(dSynTSentence, ProcessElementType.ANDJOIN, TParallelGateway.class);
    }

    private boolean isAddDiscourseMarker(DSynTSentence dSynTSentence, ProcessElementType processElementType, Class gatewayClass) {
        if (dSynTSentence.getExecutableFragment().isJoinSentence) {
            return isAddDiscourseMarkerJoinSentence(dSynTSentence, processElementType);
        } else {
            return isAddJoinDiscourseMarkerNormalSentence(dSynTSentence, gatewayClass);
        }
    }

    private boolean isAddDiscourseMarkerJoinSentence(DSynTSentence dSynTSentence, ProcessElementType processElementType) {
        for (ProcessElementDocument processElementDocument : dSynTSentence.getProcessElementDocumentList()) {
            if (processElementDocument.getProcessElementType().equals(processElementType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAddJoinDiscourseMarkerNormalSentence(DSynTSentence dSynTSentence, Class gatewayClass) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (gatewayClass.isInstance(tFlowElementSource)) {
                        if (bpmnWrapper.isGatewayJoin((TGateway) tFlowElementSource)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isAddFinalDiscourseMarker(int index, int textPlanSize) {
        return index == textPlanSize - 1;
    }

    private void insertOrdinalConnective(DSynTSentence dSynTSentence, int currentLevel) {
        int ordinalInt = levelOrdinalMap.get(currentLevel);
        String lexeme = Lexemes.ORDINAL_CONNECTIVE.replaceAll("@ordinal", getOrdinal(ordinalInt));
        insertConnective(dSynTSentence, lexeme);
    }

    private String getOrdinal(int ordinalInt) {
        RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
        return ruleBasedNumberFormat.format(ordinalInt, "%spellout-ordinal");
    }

    private void adjustOrdinalIndex(int currentLevel) {
        if (currentLevel == lastLevel) {
            int currentOrdinal = getCurrentOrdinalIndex(currentLevel);
            levelOrdinalMap.put(currentLevel, currentOrdinal + 1);
        } else if (currentLevel > lastLevel) {
            levelOrdinalMap.put(currentLevel, 1);
        } else {
            removeSubLevels(currentLevel);
            int currentOrdinal = getCurrentOrdinalIndex(currentLevel);
            levelOrdinalMap.put(currentLevel, currentOrdinal + 1);
        }
    }

    private int getCurrentOrdinalIndex(int currentLevel) {
        if (!levelOrdinalMap.containsKey(currentLevel)) {
            levelOrdinalMap.put(currentLevel, 1);
        }
        return levelOrdinalMap.get(currentLevel);
    }

    private void removeSubLevels(int currentLevel) {
        Iterator iterator = levelOrdinalMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            int keyLevel = (int) pair.getKey();
            if (keyLevel > currentLevel) {
                iterator.remove();
            }
        }
    }

    private void insertParallelConnective(DSynTSentence dSynTSentence) {
        insertConnective(dSynTSentence, Lexemes.PARALLEL_CONNECTIVES.get(indexParallelConnectors));
        adjustIndexParallelConnectors();
    }

    private void insertSequentialConnective(DSynTSentence dSynTSentence) {
        insertConnective(dSynTSentence, Lexemes.SEQUENCE_CONNECTIVES.get(indexSequentialConnectors));
        adjustIndexSequentialConnectors();
    }

    private void insertConnective(DSynTSentence dSynTSentence, String lexeme) {
        Element verb = dSynTSentence.getVerb();
        Document doc = dSynTSentence.getDSynT();
        IntermediateToDSynTConverter.insertConnective(doc, verb, lexeme);
    }

    private void adjustIndexSequentialConnectors() {
        indexSequentialConnectors = findNextIndexConnector(indexSequentialConnectors, Lexemes.SEQUENCE_CONNECTIVES.size());
    }

    private void adjustIndexParallelConnectors() {
        indexParallelConnectors = findNextIndexConnector(indexParallelConnectors, Lexemes.PARALLEL_CONNECTIVES.size());
    }

    private int findNextIndexConnector(int indexConnector, int size) {
        indexConnector++;
        if (indexConnector == size) {
            indexConnector = 0;
        }
        return indexConnector;
    }

}
