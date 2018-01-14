package processToText.contentDetermination.labelAnalysis.interfaces;

import processToText.contentDetermination.labelAnalysis.structure.Activity;

import java.util.ArrayList;

public interface LabelDeriver {

    /**
     * Investigates label and determines action and business object.
     */
    void processLabel(Activity label, String labelStyle);

    /**
     * Returns the computed action of the processed label.
     */
    ArrayList<String> returnActions();

    /**
     * Returns the computed business object of the processed label.
     */
    ArrayList<String> returnBusinessObjects();

    /**
     * Returns the computed addition.
     */
    String returnAddition();

}
