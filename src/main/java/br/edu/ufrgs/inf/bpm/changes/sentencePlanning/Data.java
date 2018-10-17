package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.ExecutableFragment;

public class Data {
    private String role;
    private ExecutableFragment fragment;
    private DSynTSentence dSynTSentence;

    public Data() {
    }

    public Data(DSynTSentence dSynTSentence) {
        role = dSynTSentence.getExecutableFragment().getRole();
        fragment = dSynTSentence.getExecutableFragment();
        this.dSynTSentence = dSynTSentence;
    }

    public void setValues(Data data) {
        role = data.role;
        fragment = data.fragment;
        dSynTSentence = data.dSynTSentence;
    }

    public void cleanData() {
        role = null;
        fragment = null;
        dSynTSentence = null;
    }

    public String getRole() {
        return role;
    }

    public ExecutableFragment getFragment() {
        return fragment;
    }

    public DSynTSentence getdSynTSentence() {
        return dSynTSentence;
    }
}