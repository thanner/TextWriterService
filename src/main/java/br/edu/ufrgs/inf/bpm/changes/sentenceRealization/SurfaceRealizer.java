package br.edu.ufrgs.inf.bpm.changes.sentenceRealization;

import br.edu.ufrgs.inf.bpm.builder.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.metatext.TSentence;
import br.edu.ufrgs.inf.bpm.metatext.TSnippet;
import br.edu.ufrgs.inf.bpm.metatext.TText;
import com.cogentex.real.api.RealProMgr;
import org.w3c.dom.Document;
import processToText.dataModel.dsynt.DSynTSentence;

import java.util.ArrayList;
import java.util.List;


public class SurfaceRealizer {

    int c = 0;
    private RealProMgr realproManager;

    public SurfaceRealizer() {
        realproManager = new RealProMgr();
    }

    public TText generateText(ArrayList<DSynTSentence> sentencePlan) {
        TText text = new TText();
        for (DSynTSentence s : sentencePlan) {
            TSentence sentence = new TSentence();
            sentence.setLevel(s.getExecutableFragment().sen_level);
            sentence.setIslateral(s.getExecutableFragment().sen_hasBullet);
            sentence.setValue(realizeSentence(s.getDSynT()));
            sentence.getSnippetList().addAll(getSnippetList(s, sentence.getValue()));
            text.getSentenceList().add(sentence);
        }

        return text;
    }

    private List<TSnippet> getSnippetList(DSynTSentence s, String sentence) {
        List<TSnippet> subsentenceList = new ArrayList<>();
        for (ProcessElementDocument processElementDocument : s.getProcessElementDocumentList()) {
            TSnippet subsentence = new TSnippet();
            subsentence.setProcessElementId(processElementDocument.getProcessElementId());
            subsentence.setProcessElementType(processElementDocument.getProcessElementType());
            subsentence.setResource(processElementDocument.getResource());

            String subsentenceText = processElementDocument.getSentence();
            subsentence.setStartIndex(getIndexStartSubstentence(sentence, subsentenceText));
            subsentence.setEndIndex(getIndexEndSubsentence(subsentence.getStartIndex(), postProcessText(subsentenceText)));

            subsentenceList.add(subsentence);
        }

        return subsentenceList;
    }

    public TText postProcessText(TText text) {
        for (TSentence sentence : text.getSentenceList()) {
            sentence.setValue(postProcessText(sentence.getValue()));
        }
        return text;
    }

    // Realize Sentence
    public String realizeSentence(Document document){
        realproManager.realize(document);
        String realized = realproManager.getSentenceString();
        return realized != null ? realized : "";
    }

    private int getIndexStartSubstentence(String sentence, String subsentence){
        return postProcessText(sentence).toLowerCase().indexOf(postProcessText(subsentence).toLowerCase());
    }

    private int getIndexEndSubsentence(int indexStart, String subsentence){
        return indexStart + subsentence.length();
    }

    public String postProcessText(String surfaceText) {
        surfaceText = surfaceText.replaceAll("If it is necessary", "If it is necessary,");
        surfaceText = surfaceText.replaceAll("one of the branches was executed", "one of the branches was executed,");
        surfaceText = surfaceText.replaceAll("In concurrency to the latter steps", "In concurrency to the latter steps,");
        surfaceText = surfaceText.replaceAll("Once both branches were finished", "Once both branches were finished,");
        surfaceText = surfaceText.replaceAll("Once the loop is finished", "Once the loop is finished,");
        surfaceText = surfaceText.replaceAll("one of the following branches is executed.", "one of the following branches is executed:");
        surfaceText = surfaceText.replaceAll("one or more of the following branches is executed.", "one or more of the following branches is executed:");
        surfaceText = surfaceText.replaceAll("parallel branches.", "parallel branches:");
        surfaceText = surfaceText.replaceAll("If it is required", "If it is required,");
        surfaceText = surfaceText.replaceAll(" the a ", " a ");
        surfaceText = surfaceText.replaceAll("branches were executed ", "branches were executed, ");

        return surfaceText;
    }

    public String cleanTextForImperativeStyle(String surfaceText, String imperativeRole, ArrayList<String> roles) {
        if (surfaceText.contains("the " + imperativeRole)) {
            surfaceText = surfaceText.replaceAll("the " + imperativeRole, "you");
        }
        if (surfaceText.contains("The " + imperativeRole)) {
            surfaceText = surfaceText.replaceAll("The " + imperativeRole, "you");
        }
        if (surfaceText.contains("the " + imperativeRole.toLowerCase())) {
            surfaceText = surfaceText.replaceAll("the " + imperativeRole.toLowerCase(), "you");
        }
        if (surfaceText.contains("The " + imperativeRole.toLowerCase())) {
            surfaceText = surfaceText.replaceAll("The " + imperativeRole.toLowerCase(), "you");
        }
        if (surfaceText.contains(imperativeRole.toLowerCase())) {
            surfaceText = surfaceText.replaceAll(imperativeRole.toLowerCase(), "you");
        }
        if (surfaceText.contains(imperativeRole)) {
            surfaceText = surfaceText.replaceAll(imperativeRole, "you");
        }
        for (String role : roles) {
            if (surfaceText.contains("and " + role.toLowerCase())) {
                surfaceText = surfaceText.replaceAll("and " + role.toLowerCase(), "and the " + role.toLowerCase());
            }
            if (surfaceText.contains("and " + role)) {
                surfaceText = surfaceText.replaceAll("and " + role, "and the " + role);
            }
        }
        return surfaceText;
    }

}
