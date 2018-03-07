package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.ExecutableFragment;

import java.util.ArrayList;

public class SentenceAggregator {

    private class Data{
        private String role;
        private ExecutableFragment fragment;
        private DSynTSentence sentence;

        private Data(){}

        private Data(DSynTSentence dSynTSentence){
            role = dSynTSentence.getExecutableFragment().getRole();
            fragment = dSynTSentence.getExecutableFragment();
            sentence = dSynTSentence;
        }

        private void setValues(Data data){
            role = data.role;
            fragment = data.fragment;
            sentence = data.sentence;
        }

        private void cleanData(){
            role = null;
            fragment = null;
            sentence= null;
        }
    }

    public ArrayList<DSynTSentence> performRoleAggregation(ArrayList<DSynTSentence> textPlan) {
        ArrayList<Integer> toBeDeleted = new ArrayList<>();
        int deleteCount = 0;

        Data previousData = new Data();

        for (int i = 0; i < textPlan.size(); i++) {
            Data currentData = new Data(textPlan.get(i));

            if (i > 1 && previousData.role != null && previousData.fragment != null && previousData.sentence != null) {
                if (isAggregation(previousData, currentData)) {

                    // Create list with sentences which need to be aggregated with the current one
                    ArrayList<DSynTMainSentence> coordSentences = new ArrayList<>();
                    coordSentences.add((DSynTMainSentence) currentData.sentence);

                    // Conduct role aggregation
                    ((DSynTMainSentence) previousData.sentence).addCoordSentences(coordSentences);

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

    private boolean isAggregation(Data previousData, Data currentData){
        String dSynTSentenceClass = "class processToText.dataModel.dsynt.DSynTMainSentence";
        return currentData.role.equals(previousData.role) && !currentData.role.equals("") &&
                !currentData.fragment.sen_hasBullet && currentData.fragment.sen_level == previousData.fragment.sen_level &&
                previousData.sentence.getExecutableFragment().getListSize() == 0 &&
                !currentData.fragment.sen_hasConnective && !previousData.fragment.sen_hasConnective &&
                currentData.sentence.getClass().toString().equals(dSynTSentenceClass) &&
                previousData.sentence.getClass().toString().equals(dSynTSentenceClass);
    }

}