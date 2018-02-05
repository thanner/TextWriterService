package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.util.Paths;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class Adjustments {

    // /Users/thanner/IdeaProjects/ProcessToTextRest/out/artifacts/ProcessToTextRest_war_exploded/WEB-INF/classes/wordNet/dict

    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String artifactDict = "/out/artifacts/ProcessToTextRest_war_exploded/WEB-INF/classes/wordNet/dict";
        String localDict = "/resources/wordNet/dict";

        String dicionaryPath = currentDir + localDict;
        changeWordNetDictionayPath(Paths.LocalResourcePath + Paths.WordNetPath, dicionaryPath);
    }

    private static void changeWordNetDictionayPath(String wordNetPath, String dicionaryPath){
        try {
            File file = new File(wordNetPath);
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            NodeList nList = document.getElementsByTagName("jwnl_properties");
            for (int i = 0; i < nList.getLength(); i++)
            {
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;
                Element cElement =  (Element) eElement.getElementsByTagName("dictionary").item(0);

                NodeList nodeList = cElement.getElementsByTagName("param");
                for(int j = 0; j < nodeList.getLength(); j++){
                    Element elementJ = (Element) nodeList.item(j);
                    if(elementJ.getAttribute("name").equals("dictionary_path")){
                        elementJ.setAttribute("value", dicionaryPath);
                    }
                }
            }

            writeXml(document, file);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static void writeXml(Document document, File file){
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file.getPath());
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

}
