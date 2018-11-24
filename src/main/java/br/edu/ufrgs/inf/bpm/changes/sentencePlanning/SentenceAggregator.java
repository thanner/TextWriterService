package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;

import java.util.ArrayList;

public class SentenceAggregator {

    public ArrayList<DSynTSentence> performRoleAggregation(ArrayList<DSynTSentence> textPlan) {
        ArrayList<Integer> toBeDeleted = new ArrayList<>();
        int deleteCount = 0;

        Data previousData = new Data();

        for (int i = 0; i < textPlan.size(); i++) {
            Data currentData = new Data(textPlan.get(i));

            if (i > 1 && previousData.getRole() != null && previousData.getFragment() != null && previousData.getdSynTSentence() != null) {
                if (isAggregation(previousData, currentData)) {

                    // Create list with sentences which need to be aggregated with the current one
                    ArrayList<DSynTMainSentence> coordSentences = new ArrayList<>();
                    coordSentences.add((DSynTMainSentence) currentData.getdSynTSentence());

                    // Conduct role aggregation
                    ((DSynTMainSentence) previousData.getdSynTSentence()).addCoordSentences(coordSentences);

                    // Prepare to be deleted
                    toBeDeleted.add(i - deleteCount);
                    deleteCount++;

                    previousData.cleanData();
                } else {
                    previousData.setValues(currentData);
                }
            } else {
                previousData.setValues(currentData);
            }
        }

        for (int i : toBeDeleted) {
            textPlan.remove(i);
        }

        return textPlan;
    }

    private boolean isAggregation(Data previousData, Data currentData) {
        return //currentData.getRole().equals(previousData.getRole()) &&
                !currentData.getRole().equals("") &&
                        !currentData.getFragment().sen_hasBullet &&
                        currentData.getFragment().sen_level == previousData.getFragment().sen_level &&
                        previousData.getdSynTSentence().getExecutableFragment().getListSize() == 0 &&
                        !currentData.getFragment().sen_hasConnective && !previousData.getFragment().sen_hasConnective &&
                        currentData.getdSynTSentence().getdSynTSentenceType().equals(DSynTSentenceType.MAIN) &&
                        previousData.getdSynTSentence().getdSynTSentenceType().equals(DSynTSentenceType.MAIN) &&
                        !previousData.getFragment().isJoinSentence && !previousData.getFragment().isIndividualSentence;
    }

}