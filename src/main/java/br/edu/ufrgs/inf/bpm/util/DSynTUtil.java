package br.edu.ufrgs.inf.bpm.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DSynTUtil {

    public static Document getDSynTDocument(Node node) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document newDocument = builder.newDocument();

            Node importedNode = newDocument.importNode(node, true);
            Node parentNode = newDocument.createElement("dsynts");
            parentNode.appendChild(importedNode);
            newDocument.appendChild(parentNode);

            return newDocument;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

}
