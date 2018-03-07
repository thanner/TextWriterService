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
                    insertConnective(dSynTMainSentence);
                }
            }

            if (dSynTSentence.getdSynTSentenceType().equals(DSynTSentenceType.CONDITION)) {
                DSynTConditionSentence dSynTConditionSentence = (DSynTConditionSentence) dSynTSentence;
                if (!dSynTConditionSentence.getExecutableFragment().sen_hasConnective && index > 0 && !dSynTConditionSentence.getConditionFragment().sen_headPosition) {
                    insertConnective(dSynTConditionSentence);
                }
            }

        }

        return textPlan;
    }

    private void insertConnective(DSynTSentence dSynTSentence){
        Element verb = dSynTSentence.getVerb();
        Document doc = dSynTSentence.getDSynT();
        // Insert sequence connective
        // if (index == textPlan.size()-1) {
        //	IntermediateToDSynTConverter.insertConnective(doc, verb, "finally");
        // } else {
        IntermediateToDSynTConverter.insertConnective(doc, verb, Lexemes.SEQ_CONNECTIVES.get(indexConnectors));
        adjustIndexConnectors();
        // }
    }

    private void adjustIndexConnectors(){
        indexConnectors++;
        if (indexConnectors == Lexemes.SEQ_CONNECTIVES.size()) {
            indexConnectors = 0;
        }
    }

}
