package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

public class WordNetWrapper {

    private static Dictionary dictionary;

    public static Dictionary getDictionary(){
        if(dictionary == null){
            generateDictionary();
        }
        return dictionary;
    }

    public static void generateDictionary() {
        try {
            URL _url = WordNetWrapper.class.getResource("/WordNet/WordNet_properties.xml");
            if (_url == null) {
                File _f = new File("resources/WordNet/WordNet_properties.xml"); // "src\\main\\resources\\WordNet\\WordNet_properties.xml"
                JWNL.initialize(new FileInputStream(_f));
            } else {
                JWNL.initialize(_url.openStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dictionary = Dictionary.getInstance();
    }

}
