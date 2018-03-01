package br.edu.ufrgs.inf.bpm.changes.sentenceRealization;

import br.edu.ufrgs.inf.bpm.ProcessElementDocument;
import br.edu.ufrgs.inf.bpm.util.DSynTUtil;
import com.cogentex.real.api.RealProMgr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SurfaceRealizer {

    int c = 0;
    private RealProMgr realproManager;

    public SurfaceRealizer() {
        realproManager = new RealProMgr();
    }

    public static void printDocument(Document doc, OutputStream out) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String realizeSentenceMap(ArrayList<DSynTSentence> sentencePlan, HashMap<Integer, String> map) {
        String s = "<text>\n";
        for (DSynTSentence dsynt : sentencePlan) {
            s = s + " " + realizeMapSentence(dsynt, map) + "\n";
        }
        return s + "</text>";
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

    public String realizePlan(ArrayList<DSynTSentence> sentencePlan) {
        StringBuilder surfaceText = new StringBuilder();
        int lastLevel = -1;
        surfaceText.append("<text>");
        for (DSynTSentence s : sentencePlan) {
            int level = s.getExecutableFragment().sen_level;

            String resource = s.getExecutableFragment().getRole().trim();
            String processElement = s.getProcessElementDocumentList().get(0).getProcessElement();
            int newLineAmount = getNewLineAmount(s, level, lastLevel);
            int tabAmount = getTabAmount(s, level, lastLevel);
            boolean hasBulletPoint = getHasBulletPoint(s);

            String newSentence = realizeSentence(s);

            System.out.println();
            System.out.println(newSentence);
            printDocument(s.getDSynT(), System.out);

            List<String> subsentences = realizeSubsentences(s);

            String setenceXML = "<sentence "
                    + "resource=\"" + resource + "\" "
                    + "processElementList=\"" + processElement + "\" "
                    + "newLineAmount=\"" + newLineAmount + "\" "
                    + "tabAmount=\"" + tabAmount + "\" "
                    + "hasBulletPoint=\"" + hasBulletPoint + "\" "
                    + ">" + newSentence
                    + "</sentence>";

            // newSentence = getIdentation(s, level, lastLevel) + newSentence;

            // System.out.println("Sentença: " + newSentence);
            // s.getExecutableFragment().getAssociatedActivities().forEach(System.out::println);
            // System.out.print("\n");

            // surfaceText.append("<space/>").append(newSentence);
            surfaceText.append(setenceXML);
            lastLevel = level;
        }
        surfaceText.append("</text>");
        return surfaceText.toString();
    }

    private String getIdentation(DSynTSentence s, int level, int lastLevel) {
        String output = "";
        if (level != lastLevel || s.getExecutableFragment().sen_hasBullet) {
            output = output + "<newline/>";
            for (int i = 1; i <= level; i++) {
                output = output + "<tab/>";
            }
        }
        if (s.getExecutableFragment().sen_hasBullet) {
            output = output + "<bulletpoint/>";
        }
        c++;
        return output;
    }

    // Realize Sentence
    private String realizeSentence(DSynTSentence s) {
        Document xmldoc = s.getDSynT();
        realproManager.realize(xmldoc);
        String sentenceString = realproManager.getSentenceString();

        /**
         System.out.println("\n====================================================================================");
         // Root
         try {
         System.out.println(sentenceString);
         printDocument(xmldoc, System.out);
         System.out.println("");
         } catch (Exception e) {
         e.printStackTrace();
         }
         // Print all nodes
         printNodes(xmldoc);
         System.out.println("");
         */

        return sentenceString;
    }

    private List<String> realizeSubsentences(DSynTSentence s) {
        for (ProcessElementDocument processElementDocument : s.getProcessElementDocumentList()) {
            String processElement = processElementDocument.getProcessElement();
            Document document = processElementDocument.getDocument();

            System.out.println(processElement);
            printDocument(document, System.out);
        }
        return null;
    }

    /*
    // TODO: trabalhando aqui para tentar gerar partes de sentença
    // TODO: Remover o child quando ele for um verb (verb são novas subsentenças)
    private void printNodes(Node node) {
        // Print subsentence
        if (isNewSubsentence(node)) {
            try {
                Node mainNode = removeOtherSubsentences(node);
                Document document = DSynTUtil.getDSynTDocument(mainNode);
                realproManager.realize(document);

                // PRINTS
                System.out.println("Attribute: " + node.getAttributes().getNamedItem("rel"));
                System.out.println("Node: " + realproManager.getSentenceString());
                System.out.print("XML: ");
                printDocument(document, System.out);

            } catch (Exception e) {
                System.out.println("Node: Not defined");
            }
            System.out.println();
        }

        // NEW NODES
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                printNodes(childNode);
            }
        }

    }

    private boolean isDsyntRootNode(Node node) {
        return node.getNodeName().equals("dsynts");
    }

    private boolean isNewSubsentence(Node node) {
        try {
            return node.getAttributes().getNamedItem("class").getNodeValue().equals("verb");
        } catch (Exception e) {
            return false;
        }
    }

    private Node removeOtherSubsentences(Node node) {
        Node mainNode = node.cloneNode(true);

        if (mainNode.hasChildNodes()) {
            NodeList childNodes = mainNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (isNewSubsentenceDeep(childNode)) {
                    mainNode.removeChild(childNode);
                }
            }
        }

        return mainNode;
    }

    private boolean isNewSubsentenceDeep(Node node) {
        if (isNewSubsentence(node)) {
            return true;
        }

        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (isNewSubsentenceDeep(childNode)) {
                    return true;
                }
            }
        }

        return false;
    }
    */

    private String realizeMapSentence(DSynTSentence s, HashMap<Integer, String> map) {
        Document xmldoc = s.getDSynT();
        realproManager.realize(xmldoc);
        ArrayList<Integer> ids = s.getExecutableFragment().getAssociatedActivities();
        if (s.getClass().toString().endsWith("DSynTConditionSentence")) {
            DSynTConditionSentence cs = (DSynTConditionSentence) s;
            ids.addAll(cs.getConditionFragment().getAssociatedActivities());
            ArrayList<ConditionFragment> sentences = cs.getConditionFragment().getSentenceList();
            if (sentences != null) {
                for (ConditionFragment cFrag : sentences) {
                    ids.addAll(cFrag.getAssociatedActivities());
                }
            }
        } else {
            DSynTMainSentence ms = (DSynTMainSentence) s;
            ArrayList<ExecutableFragment> sentences = ms.getExecutableFragment().getSentencList();
            if (sentences != null) {
                for (ExecutableFragment eFrag : sentences) {
                    ids.addAll(eFrag.getAssociatedActivities());
                }
            }
        }
        String output = "";
        c++;
        String idAttr = "";
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                idAttr = idAttr + ",";
            }
            idAttr = idAttr + map.get(ids.get(i));
        }

        return output + "<phrase ids=\"" + idAttr + "\"> " + realproManager.getSentenceString() + " </phrase>";
    }

    private String realizeFragment(ConditionFragment cFrag) {
        Document xmldoc = new DSynTConditionSentence(new ExecutableFragment("", "", "", ""), cFrag).getDSynT();
        realproManager.realize(xmldoc);
        return realproManager.getSentenceString();
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
