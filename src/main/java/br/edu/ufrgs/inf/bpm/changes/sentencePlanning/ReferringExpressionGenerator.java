package br.edu.ufrgs.inf.bpm.changes.sentencePlanning;

import br.edu.ufrgs.inf.bpm.type.DSynTSentenceType;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;

import java.util.ArrayList;
import java.util.Iterator;

public class ReferringExpressionGenerator {

    private EnglishLabelHelper lHelper;

    public ReferringExpressionGenerator(EnglishLabelHelper lHelper) {
        this.lHelper = lHelper;
    }

    public ArrayList<DSynTSentence> insertReferringExpressions(ArrayList<DSynTSentence> textPlan, boolean isMale) {
        Data previousData = new Data();

        for (int i = 0; i < textPlan.size(); i++) {
            Data currentData = new Data(textPlan.get(i));
            if (previousData.getRole() != null && previousData.getFragment() != null && previousData.getdSynTSentence() != null) {
                if (isReferringExpression(previousData, currentData)) {
                    setReferringExpression(currentData, isMale);
                    // System.out.println("Referring Expression inserted: " + textPlan.get(i).getExecutableFragment().getAction() + " - " + textPlan.get(i).getExecutableFragment().getBo());
                    //previousData.cleanData();
                }
            }
            //} else {
            previousData.setValues(currentData);
            //}
        }
        return textPlan;
    }

    private void setReferringExpression(Data currentData, boolean isMale) {
        if (isPerson(currentData.getRole()) && isMale) {
            currentData.getFragment().setRole("he");
        } else {
            currentData.getFragment().setRole("it");
        }

        ((DSynTMainSentence) currentData.getdSynTSentence()).changeRole();
    }

    private boolean isReferringExpression(Data previousData, Data currentData) {
        return currentData.getRole().equals(previousData.getRole()) &&
                !currentData.getRole().equals("") && !currentData.getRole().equals("he") &&
                !currentData.getRole().equals("she") && !currentData.getRole().equals("it") &&
                !currentData.getFragment().sen_hasBullet && currentData.getFragment().sen_level == previousData.getFragment().sen_level &&
                previousData.getdSynTSentence().getExecutableFragment().getListSize() == 0 &&
                !currentData.getFragment().sen_hasConnective && !previousData.getFragment().sen_hasConnective &&
                currentData.getdSynTSentence().getdSynTSentenceType().equals(DSynTSentenceType.MAIN) &&
                previousData.getdSynTSentence().getdSynTSentenceType().equals(DSynTSentenceType.MAIN);
    }

    // Checks WordNetWrapper HypernymTree whether "role" is a person
    private boolean isPerson(String role) {
        try {
            IndexWord word = lHelper.getDictionary().getIndexWord(POS.NOUN, role.toLowerCase());
            if (word != null) {
                Synset[] senses = word.getSenses();
                for (Synset sense : senses) {
                    PointerTargetTree relatedTree = PointerUtils.getInstance().getHypernymTree(sense);
                    PointerTargetNodeList[] relatedLists = relatedTree.reverse();
                    for (PointerTargetNodeList relatedList : relatedLists) {
                        Iterator iterator = relatedList.iterator();
                        while (iterator.hasNext()) {
                            PointerTargetNode elem = (PointerTargetNode) iterator.next();
                            Synset syns = elem.getSynset();
                            for (int j = 0; j < syns.getWords().length; j++) {
                                if (syns.getWord(j).getLemma().equals("person")) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
