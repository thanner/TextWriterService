package br.edu.ufrgs.inf.bpm.changes.templates;

import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class TemplateLoader {

    private String action = "";
    private String object = "";
    private String addition = "";

    public void loadTemplate(TemplateLoaderType template) {
        action = "";
        object = "";
        addition = "";
        try {
            File tempFile = ResourceLoader.getResourceFile(Paths.SentenceTemplatePath + "/" + template.getTemplateLoader(), ".xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(tempFile);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("action");
            action = nodes.item(0).getTextContent();

            nodes = doc.getElementsByTagName("object");
            object = nodes.item(0).getTextContent();

            nodes = doc.getElementsByTagName("addition");
            addition = nodes.item(0).getTextContent();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getAddition() {
        return addition;
    }

    public void setAddition(String addition) {
        this.addition = addition;
    }

}
