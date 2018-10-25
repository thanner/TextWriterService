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

    public TText postProcessText(TText tText) {
        for (TSentence tSentence : tText.getSentenceList()) {
            tSentence.setValue(postProcessText(tSentence.getValue()));
        }
        return tText;
    }

    public TText generateText(ArrayList<DSynTSentence> sentencePlan) {
        TText tText = new TText();
        for (DSynTSentence s : sentencePlan) {
            TSentence sentence = new TSentence();
            sentence.setLevel(s.getExecutableFragment().sen_level);
            sentence.setLateral(s.getExecutableFragment().sen_hasBullet);
            sentence.setValue(realizeSentence(s.getDSynT()));
            sentence.getSnippetList().addAll(getSnippetList(s, sentence.getValue()));
            tText.getSentenceList().add(sentence);
        }

        return tText;
    }

    private List<TSnippet> getSnippetList(DSynTSentence s, String sentence) {
        List<TSnippet> snippetList = new ArrayList<>();
        for (ProcessElementDocument processElementDocument : s.getProcessElementDocumentList()) {
            TSnippet snippet = new TSnippet();
            snippet.setProcessElementId(processElementDocument.getProcessElementId());
            snippet.setProcessElementType(processElementDocument.getProcessElementType());

            snippet.setStartIndex(getIndexStart(sentence, processElementDocument));
            snippet.setEndIndex(getIndexEnd(snippet.getStartIndex(), processElementDocument));

            if (snippet.getStartIndex() == null && snippet.getEndIndex() == null) {
                snippet.setStartIndex(0);
                snippet.setEndIndex(0);
            }

            snippetList.add(snippet);
        }

        return snippetList;
    }

    // Realize Sentence
    public String realizeSentence(Document document) {
        realproManager.realize(document);
        String realized = realproManager.getSentenceString();
        return realized != null ? realized : "";
    }

    private Integer getIndexStart(String sentence, ProcessElementDocument processElementDocument) {
        String originalSentence = getSentenceForIndex(sentence).toLowerCase();
        String sentenceSnippet = getSentenceForIndex(processElementDocument.getSentence()).toLowerCase();
        Integer integer = originalSentence.indexOf(sentenceSnippet);
        if (integer == -1) {
            integer = null;
        }

        return integer;
    }

    private Integer getIndexEnd(Integer indexStart, ProcessElementDocument processElementDocument) {
        String sentenceSnippet = getSentenceForIndex(processElementDocument.getSentence());
        if (indexStart == null) {
            return null;
        } else {
            return indexStart + sentenceSnippet.length();
        }
    }

    private String getSentenceForIndex(String sentence) {
        return postProcessText(sentence).trim();
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

        surfaceText = surfaceText.replaceAll("if required", "if required,");
        surfaceText = surfaceText.replaceAll("If required", "If required,");

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
