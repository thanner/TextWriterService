package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.textPlanning.IntermediateToDSynTConverter;

import java.util.*;

public class DiscourseMarker {

    private Map<Integer, Integer> levelOrdinalMap = new HashMap<>();
    private int lastLevel = -1;

    private int indexConnectors = 0;

    public ArrayList<DSynTSentence> insertSequenceConnectives(ArrayList<DSynTSentence> textPlan) {
        for (int index = 0; index < textPlan.size(); index++) {

            DSynTSentence dSynTSentence = textPlan.get(index);
            int currentLevel = dSynTSentence.getExecutableFragment().sen_level;

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.MAIN)) {
                DSynTMainSentence dSynTMainSentence = (DSynTMainSentence) dSynTSentence;
                if (!dSynTMainSentence.getExecutableFragment().sen_hasConnective && index > 0) {
                    // Is a parallel sequence (first, second, third)
                    if (dSynTMainSentence.getExecutableFragment().sen_hasBullet) {
                        adjustOrdinalIndex(currentLevel);
                        insertOrdinalConnective(dSynTMainSentence, currentLevel);
                        // Is a sequence (then, next, after)
                    } else {
                        insertConnective(dSynTMainSentence, index, textPlan.size());
                    }
                }

                lastLevel = currentLevel;
            }

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.CONDITION)) {
                DSynTConditionSentence dSynTConditionSentence = (DSynTConditionSentence) dSynTSentence;
                if (!dSynTConditionSentence.getExecutableFragment().sen_hasConnective && index > 0 && !dSynTConditionSentence.getConditionFragment().sen_headPosition) {
                    insertConnective(dSynTConditionSentence, index, textPlan.size());
                }
            }

        }

        return textPlan;
    }

    private void adjustOrdinalIndex(int currentLevel) {
        if (currentLevel == lastLevel) {
            int currentOrdinal = getCurrentOrdinal(currentLevel);
            levelOrdinalMap.put(currentLevel, currentOrdinal + 1);
        } else if (currentLevel > lastLevel) {
            levelOrdinalMap.put(currentLevel, 1);
        } else {
            removeSubLevels(currentLevel);
            int currentOrdinal = getCurrentOrdinal(currentLevel);
            levelOrdinalMap.put(currentLevel, currentOrdinal + 1);
        }
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

    private int getCurrentOrdinal(int currentLevel) {
        if (!levelOrdinalMap.containsKey(currentLevel)) {
            levelOrdinalMap.put(currentLevel, 1);
        }
        return levelOrdinalMap.get(currentLevel);
    }

    private void insertOrdinalConnective(DSynTSentence dSynTSentence, int currentLevel) {
        Element verb = dSynTSentence.getVerb();
        Document doc = dSynTSentence.getDSynT();
        int ordinalInt = levelOrdinalMap.get(currentLevel);
        String lemma = Lexemes.LATERAL_CONNECTIVE.replaceAll("@ordinal", getOrdinal(ordinalInt));
        IntermediateToDSynTConverter.insertConnective(doc, verb, lemma);
    }

    private String getOrdinal(int ordinalInt) {
        RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
        return ruleBasedNumberFormat.format(ordinalInt, "%spellout-ordinal");
    }

    private void insertConnective(DSynTSentence dSynTSentence, int index, int textPlanSize){
        Element verb = dSynTSentence.getVerb();
        Document doc = dSynTSentence.getDSynT();
        if (isLastSentenceText(index, textPlanSize)) {
        	IntermediateToDSynTConverter.insertConnective(doc, verb, Lexemes.SEQUENCEFINAL_CONNECTIVE);
        } else {
            IntermediateToDSynTConverter.insertConnective(doc, verb, Lexemes.SEQUENCE_CONNECTIVES.get(indexConnectors));
        }
        adjustIndexConnectors();
    }

    private boolean isLastSentenceText(int index, int textPlanSize){
        return index == textPlanSize - 1;
    }

    private void adjustIndexConnectors(){
        indexConnectors++;
        if (indexConnectors == Lexemes.SEQUENCE_CONNECTIVES.size()) {
            indexConnectors = 0;
        }
    }

}
