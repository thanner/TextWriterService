package processToText.contentDetermination.labelAnalysis.interfaces;

import processToText.contentDetermination.labelAnalysis.structure.Activity;

import java.util.ArrayList;
import java.util.HashMap;

public interface LabelCategorizer {

    /**
     * Returns the label style of the given activity / model collection.
     * Possible results (English models): 'AN', 'VO'
     * Possible results (Dutch models): 'AN', 'AN (first)', 'VO', 'VO (inf)', 'OI'
     */
    String getLabelStyle(Activity activity);

    HashMap<String, String> getLabelStyle(ArrayList<ArrayList<Activity>> modelCollection);

}
