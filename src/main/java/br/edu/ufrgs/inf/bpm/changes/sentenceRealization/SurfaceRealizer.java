package br.edu.ufrgs.inf.bpm.changes.sentenceRealization;

import com.cogentex.real.api.RealProMgr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class SurfaceRealizer {

    int c = 0;
    private RealProMgr realproManager;

    public SurfaceRealizer() {
        realproManager = new RealProMgr();
    }

    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
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
            String processElement = s.getProcessElement();
            int newLineAmount = getNewLineAmount(s, level, lastLevel);
            int tabAmount = getTabAmount(s, level, lastLevel);
            boolean hasBulletPoint = getHasBulletPoint(s);

            String newSentence = realizeSentence(s);

            String setenceXML = "<sentence "
                    + "resource=\"" + resource + "\" "
                    + "processElement=\"" + processElement + "\" "
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

        // Root
        try {
            System.out.println(sentenceString);
            printDocument(xmldoc, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Print all nodes
        printNodes(xmldoc);
        System.out.println("");

        return sentenceString;
    }

    // TODO: trabalhando aqui para tentar gerar partes de sentença
    private void printNodes(Node node) {
        try {
            Node namedItem = node.getAttributes().getNamedItem("rel");
            if (namedItem.getNodeValue().equals("ATTR")) {
                System.out.println("ATTR");
            }
            System.out.println(namedItem);

            Element element = (Element) node;
            realproManager.realize(element);
            System.out.println("Node: " + realproManager.getSentenceString());
        } catch (Exception e) {
            System.out.println("Not defined");
        }

        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                printNodes(childNodes.item(i));
            }
        }
    }

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
