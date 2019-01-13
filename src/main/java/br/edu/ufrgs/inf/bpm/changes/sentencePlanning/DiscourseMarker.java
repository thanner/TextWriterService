package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.omg.spec.bpmn._20100524.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;
import processToText.textPlanning.IntermediateToDSynTConverter;

import javax.xml.namespace.QName;
import java.util.*;

public class DiscourseMarker {

    private Map<Integer, Integer> levelOrdinalMap = new HashMap<>();
    private Set<String> parallelGatewayIndexSet = new HashSet<>();
    // private Set<QName> pathsVisited = new HashSet<>();

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
            // updateVisitedPaths(dSynTSentence);

            if (hasDiscourseMarker(textPlan, index)) {
                ExecutableFragment eFrag = dSynTSentence.getExecutableFragment();
                currentLevel = eFrag.sen_level;

                if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.MAIN)) {
                    insertConnectives(dSynTSentence, index, textPlan.size());
                    lastLevel = currentLevel;
                }

                if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.CONDITION)) {
                    DSynTConditionSentence dSynTConditionSentence = (DSynTConditionSentence) dSynTSentence;
                    ConditionFragment cFrag = dSynTConditionSentence.getConditionFragment();
                    if (!cFrag.sen_headPosition || cFrag.skip) {
                        insertConnectives(dSynTSentence, index, textPlan.size());
                    }
                    lastLevel = currentLevel;
                }

            }

        }

        return textPlan;
    }

    /*
    private void updateVisitedPaths(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if(tFlowElement instanceof TFlowNode) {
                //pathsVisited.addAll(((TFlowNode) tFlowElement).getIncoming());
                pathsVisited.addAll(((TFlowNode) tFlowElement).getOutgoing());
            }
        }
    }
    */

    private boolean hasDiscourseMarker(ArrayList<DSynTSentence> textPlan, int index) {
        if (index > 0) {
            ExecutableFragment currentExecutableFragment = textPlan.get(index).getExecutableFragment();
            ExecutableFragment previousExecutableFragment = textPlan.get(index - 1).getExecutableFragment();
            return currentExecutableFragment.sen_canAddDiscourseMarker && !currentExecutableFragment.sen_hasConnective && !previousExecutableFragment.isSentenceStartDecision && !previousExecutableFragment.isIndividualSentence;
        }
        return false;
    }

    private void insertConnectives(DSynTSentence dSynTSentence, int index, int textPlanSize) {
        if (isRigidSentencePath(dSynTSentence)) {
            if (isNewPath(dSynTSentence)) {
                adjustOrdinalIndex(currentLevel);
                insertOrdinalConnective(dSynTSentence, currentLevel);
            } else {
                insertSequentialConnective(index, textPlanSize, dSynTSentence);
            }
        } else if (isAddExclusiveJoinDiscourseMarker(dSynTSentence)) {
            insertJoinConnective(dSynTSentence, Lexemes.XOR_JOIN_CONNECTIVE, TExclusiveGateway.class, ProcessElementType.XORJOIN);
        } else if (isAddInclusiveJoinDiscourseMarker(dSynTSentence)) {
            insertJoinConnective(dSynTSentence, Lexemes.OR_JOIN_CONNECTIVE, TInclusiveGateway.class, ProcessElementType.ORJOIN);
        } else if (isAddParallelJoinDiscourseMarker(dSynTSentence)) {
            insertJoinConnective(dSynTSentence, Lexemes.AND_JOIN_CONNECTIVE, TParallelGateway.class, ProcessElementType.ANDJOIN);
        } else {
            // Is a lateral sequence
            if (isNewPath(dSynTSentence)) {
                // Source is a parallel gateway (in the meantime, at the same time)
                if (isParallelPathDiscourseMarker(dSynTSentence)) {
                    insertParallelConnective(dSynTSentence);
                    // Source is a exclusive/inclusive gateway
                } else if (isExclusivePathDiscourseMarkers(dSynTSentence)) {
                    if (allGatewayPathsHasLabels(dSynTSentence)) {
                        String condition = getCondition(dSynTSentence);
                        insertIfConnective(dSynTSentence, condition);
                    } else {
                        adjustOrdinalIndex(currentLevel);
                        insertOrdinalConnective(dSynTSentence, currentLevel);
                    }
                }
            } else {
                insertSequentialConnective(index, textPlanSize, dSynTSentence);
            }
        }

    }

    private String getCondition(DSynTSentence dSynTSentence) {
        TFlowNode tFlowNode = getFlowNodeHasGatewayAsSource(dSynTSentence);
        TGateway tGateway = getGatewaySource(dSynTSentence);
        assert tFlowNode != null;
        for (QName qName : tFlowNode.getIncoming()) {
            TSequenceFlow sequenceFlow = (TSequenceFlow) bpmnWrapper.getFlowElementByQName(qName);
            if (sequenceFlow.getSourceRef().equals(tGateway)) {
                return sequenceFlow.getName();
            }
        }
        return "unknown";
    }

    private boolean isRigidSentencePath(DSynTSentence dSynTSentence) {
        return dSynTSentence.getExecutableFragment().isRigidPath;
    }

    private boolean isNewPath(DSynTSentence dSynTSentence) {
        return dSynTSentence.getExecutableFragment().sen_hasBullet;
    }

    private boolean allGatewayPathsHasLabels(DSynTSentence dSynTSentence) {
        TGateway tGateway = getGatewaySource(dSynTSentence);
        if (tGateway != null) {
            return bpmnWrapper.isAllGatewayPathsHasLabels(tGateway);
        } else {
            return false;
        }
    }

    private TGateway getGatewaySource(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (tFlowElementSource instanceof TGateway) {
                        return (TGateway) tFlowElementSource;
                    }
                }
            }
        }
        return null;
    }

    private TFlowNode getFlowNodeHasGatewayAsSource(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                TFlowNode tFlowNode = (TFlowNode) tFlowElement;
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (tFlowElementSource instanceof TGateway) {
                        return tFlowNode;
                    }
                }
            }
        }
        return null;
    }

    private boolean isParallelPathDiscourseMarker(DSynTSentence dSynTSentence) {
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

    private boolean isExclusivePathDiscourseMarkers(DSynTSentence dSynTSentence) {
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TFlowNode) {
                for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                    if (tFlowElementSource instanceof TExclusiveGateway || tFlowElementSource instanceof TInclusiveGateway || tFlowElementSource instanceof TBoundaryEvent) {
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
            return isAddDiscourseMarkerJoinSentence(dSynTSentence, processElementType) && !isJoinLoop(dSynTSentence);
        } else {
            return isAddJoinDiscourseMarkerNormalSentence(dSynTSentence, gatewayClass) && !isJoinLoop(dSynTSentence);
        }
    }

    private boolean isJoinLoop(DSynTSentence dSynTSentence) {
        return dSynTSentence.getExecutableFragment().isLoop;
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

    /*
    private boolean isJoinLoop(DSynTSentence dSynTSentence){
        for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
            TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
            if (tFlowElement instanceof TGateway) {
                for(QName qName: ((TGateway) tFlowElement).getIncoming()){
                    if(!pathsVisited.contains(qName)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    */

    private boolean isAddFinalDiscourseMarker(int index, int textPlanSize) {
        return index == textPlanSize - 1;
    }

    private void insertIfConnective(DSynTSentence dSynTSentence, String condition) {
        String lexeme = Lexemes.IF_CONNECTIVE.replace("@condition", condition.trim());
        insertConnective(dSynTSentence, lexeme);
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

    private void insertSequentialConnective(int index, int textPlanSize, DSynTSentence dSynTSentence) {
        if (isAddFinalDiscourseMarker(index, textPlanSize)) {
            insertConnective(dSynTSentence, Lexemes.SEQUENCE_FINAL_CONNECTIVE);
        } else {
            insertSequentialConnective(dSynTSentence);
        }
    }

    private void insertSequentialConnective(DSynTSentence dSynTSentence) {
        insertConnective(dSynTSentence, Lexemes.SEQUENCE_CONNECTIVES.get(indexSequentialConnectors));
        adjustIndexSequentialConnectors();
    }

    private void insertJoinConnective(DSynTSentence dSynTSentence, String lexeme, Class gatewayClass, ProcessElementType processElementType) {
        TGateway tGateway = getGateway(dSynTSentence, gatewayClass, processElementType);
        String gatewayId = "";
        if (tGateway != null) {
            gatewayId = tGateway.getId();
            removeCurrentReferences(gatewayId, dSynTSentence.getProcessElementDocumentList());
        }

        ProcessElementDocument processElementDocument = new ProcessElementDocument();
        processElementDocument.setProcessElementId(gatewayId);
        processElementDocument.setProcessElementType(processElementType);
        processElementDocument.setSentence(lexeme);
        processElementDocument.setResourceName("");
        dSynTSentence.getProcessElementDocumentList().add(processElementDocument);

        insertConnective(dSynTSentence, lexeme);
    }

    private TGateway getGateway(DSynTSentence dSynTSentence, Class gatewayClass, ProcessElementType processElementType) {
        if (dSynTSentence.getExecutableFragment().isJoinSentence) {
            for (ProcessElementDocument processElementDocument : dSynTSentence.getProcessElementDocumentList()) {
                if (processElementDocument.getProcessElementType().equals(processElementType)) {
                    return (TGateway) bpmnWrapper.getFlowElementById(processElementDocument.getProcessElementId());
                }
            }
        } else {
            for (ProcessElementDocument processElement : dSynTSentence.getProcessElementDocumentList()) {
                TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(processElement.getProcessElementId());
                if (tFlowElement instanceof TFlowNode) {
                    for (TFlowElement tFlowElementSource : bpmnWrapper.getFlowNodeSourceList((TFlowNode) tFlowElement)) {
                        if (gatewayClass.isInstance(tFlowElementSource)) {
                            if (bpmnWrapper.isGatewayJoin((TGateway) tFlowElementSource)) {
                                return (TGateway) tFlowElementSource;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void removeCurrentReferences(String processElementId, List<ProcessElementDocument> processElementDocumentList) {
        Iterator iterator = processElementDocumentList.iterator();
        while (iterator.hasNext()) {
            ProcessElementDocument processElementDocument = (ProcessElementDocument) iterator.next();
            if (processElementDocument.getProcessElementId().equals(processElementId)) {
                iterator.remove();
            }
        }
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
