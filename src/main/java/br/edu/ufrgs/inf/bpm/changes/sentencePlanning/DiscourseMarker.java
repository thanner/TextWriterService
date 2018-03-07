package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.DSynTSentenceType;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.textPlanning.IntermediateToDSynTConverter;

import java.util.ArrayList;

public class DiscourseMarker {

    private int indexConnectors = 0;

    public ArrayList<DSynTSentence> insertSequenceConnectives(ArrayList<DSynTSentence> textPlan) {

        for (int index = 0; index < textPlan.size(); index++) {

            DSynTSentence dSynTSentence = textPlan.get(index);

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.MAIN)) {
                DSynTMainSentence dSynTMainSentence = (DSynTMainSentence) dSynTSentence;
                if (!dSynTMainSentence.getExecutableFragment().sen_hasConnective && index > 0 && !dSynTMainSentence.getExecutableFragment().sen_hasBullet) {
                    insertConnective(dSynTMainSentence, index, textPlan.size());
                }
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
