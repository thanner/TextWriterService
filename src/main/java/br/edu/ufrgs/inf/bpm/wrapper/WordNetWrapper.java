package br.edu.ufrgs.inf.bpm.wrapper;

import br.edu.ufrgs.inf.bpm.util.Path;
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
            URL _url = WordNetWrapper.class.getResource(Path.WordNetResourcePath);
            if (_url == null) {
                File _f = new File(Path.WordNetPath);
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
