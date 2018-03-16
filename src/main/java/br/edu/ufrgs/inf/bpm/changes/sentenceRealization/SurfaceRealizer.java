package br.edu.ufrgs.inf.bpm.changes.sentenceRealization;

import br.edu.ufrgs.inf.bpm.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.builder.IndexDocumentGenerator;
import com.cogentex.real.api.RealProMgr;
import org.w3c.dom.Document;
import processToText.dataModel.dsynt.DSynTSentence;

import java.util.ArrayList;


public class SurfaceRealizer {

    int c = 0;
    private RealProMgr realproManager;

    public SurfaceRealizer() {
        realproManager = new RealProMgr();
    }

    public String generateXMLSentence(ArrayList<DSynTSentence> sentencePlan) {
        StringBuilder surfaceText = new StringBuilder();
        int lastLevel = -1;
        boolean firstLine = true;
        IndexDocumentGenerator indexDocumentGenerator = new IndexDocumentGenerator();

        surfaceText.append("<text>");
        for (DSynTSentence s : sentencePlan) {
            int level = s.getExecutableFragment().sen_level;

            String newSentence = realizeSentence(s.getDSynT());
            String subsentenceXml = generateXmlSubsentence(s, newSentence);
            String indexSentence = indexDocumentGenerator.getIndex(level, lastLevel, s.getExecutableFragment().sen_hasBullet);

            int newLineAmount;
            if (firstLine) {
                newLineAmount = 0;
                firstLine = false;
            } else {
                newLineAmount = getNewLineAmount(s, level, lastLevel);
            }
            int tabAmount = getTabAmount(s, level, lastLevel);
            boolean hasBulletPoint = getHasBulletPoint(s);

            StringBuilder setenceXML = new StringBuilder();
            setenceXML.append("<sentence ")
                    .append("newLineAmount=\"").append(newLineAmount).append("\" ")
                    .append("tabAmount=\"").append(tabAmount).append("\" ")
                    .append("hasBulletPoint=\"").append(hasBulletPoint).append("\" ")
                    .append("indexSentence=\"").append(indexSentence).append("\" ")
                    .append("value=\"").append(newSentence).append(" \" ")
                    .append(">")
                    .append(subsentenceXml)
                    .append("</sentence>");

            surfaceText.append(setenceXML);
            lastLevel = level;

            // System.out.println(newSentence);
            // XmlFormat.printDocument(s.getDSynT());
        }
        surfaceText.append("</text>");

        return surfaceText.toString();
    }

    // Realize Sentence
    public String realizeSentence(Document document){
        realproManager.realize(document);
        return realproManager.getSentenceString();
    }

    private int getNewLineAmount(DSynTSentence s, int level, int lastLevel) {
        int newLineAmount = 0;
        if (level != lastLevel || s.getExecutableFragment().sen_hasBullet) {
            newLineAmount = 1;
        }
        return newLineAmount;
    }

    private int getTabAmount(DSynTSentence s, int level, int lastLevel) {
        int tabAmount = 0;
        if (level != lastLevel || s.getExecutableFragment().sen_hasBullet) {
            tabAmount = level;
        }
        return tabAmount;
    }

    private boolean getHasBulletPoint(DSynTSentence s) {
        return s.getExecutableFragment().sen_hasBullet;
    }

    private String generateXmlSubsentence(DSynTSentence s, String sentence) {
        StringBuilder subsentenceXML = new StringBuilder();
        for (ProcessElementDocument processElementDocument : s.getProcessElementDocumentList()) {
            String processElement = processElementDocument.getProcessElement();
            String resource = processElementDocument.getResource();

            // System.out.println("Subsentence: " + realizeSentence(document));

            String subsentence = processElementDocument.getSentence();
            int startIndex = getIndexStartSubstentence(sentence, subsentence);
            int indexEnd = getIndexEndSubsentence(startIndex, subsentence);

            subsentenceXML.append("<subsentence ")
                    .append("processElement=\"").append(processElement).append("\" ")
                    .append("resource=\"").append(resource).append("\" ")
                    .append("startIndex=\"").append(startIndex).append("\" ")
                    .append("endIndex=\"").append(indexEnd).append("\" ")
                    .append(">")
                    .append("</subsentence>");
        }

        return subsentenceXML.toString();
    }

    private int getIndexStartSubstentence(String sentence, String subsentence){
        return postProcessText(sentence).toLowerCase().indexOf(subsentence.toLowerCase());
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
