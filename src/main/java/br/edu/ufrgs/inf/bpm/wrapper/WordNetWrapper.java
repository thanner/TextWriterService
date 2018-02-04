package br.edu.ufrgs.inf.bpm.wrapper;

import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.*;

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
            InputStream resource = ResourceLoader.getResource(Paths.WordNetPath);
            JWNL.initialize(resource);
        } catch (IOException | JWNLException e) {
            e.printStackTrace();
        }

        dictionary = Dictionary.getInstance();
    }

}
