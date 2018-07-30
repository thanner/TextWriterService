package br.edu.ufrgs.inf.bpm.wrapper;

import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;

public class WordNetWrapper {

    private static Dictionary dictionary;

    public static Dictionary getDictionary() {
        if (dictionary == null) {
            generateDictionary();
        }
        return dictionary;
    }

    public static void generateDictionary() {
        try {
            changeWordNetDictionayPath();
            InputStream resource = ResourceLoader.getResource(Paths.WordNetPath);
            JWNL.initialize(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dictionary = Dictionary.getInstance();
    }

    public static void changeWordNetDictionayPath() throws IOException, TransformerException, SAXException, ParserConfigurationException {
        String filePath = ResourceLoader.getResourcePath(Paths.WordNetPath);
        String dictionaryPath = ResourceLoader.getResourcePath(Paths.WordNetDict);

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(filePath);

        NodeList nList = document.getElementsByTagName("jwnl_properties");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            Element eElement = (Element) nNode;
            Element cElement = (Element) eElement.getElementsByTagName("dictionary").item(0);

            NodeList nodeList = cElement.getElementsByTagName("param");
            for (int j = 0; j < nodeList.getLength(); j++) {
                Element elementJ = (Element) nodeList.item(j);
                if (elementJ.getAttribute("name").equals("dictionary_path")) {
                    elementJ.setAttribute("value", dictionaryPath);
                }
            }
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(filePath);
        transformer.transform(source, result);
    }

}
