package br.edu.ufrgs.inf.bpm.wrapper;

import br.edu.ufrgs.inf.bpm.util.Path;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.*;
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
            /*
            URL _url = WordNetWrapper.class.getResource(Path.WordNetResourcePath);
            if (_url == null) {
                File _f = new File(Path.WordNetPath);
                JWNL.initialize(new FileInputStream(_f));
            } else {
                JWNL.initialize(_url.openStream());
            }
            */

            InputStream resource = ResourceLoader.getResource(Path.WordNetResourcePath, Path.WordNetPath);
            JWNL.initialize(resource);
        } catch (IOException | JWNLException e) {
            e.printStackTrace();
        }

        dictionary = Dictionary.getInstance();
    }

}
