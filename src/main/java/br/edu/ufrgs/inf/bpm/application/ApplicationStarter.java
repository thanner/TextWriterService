package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;

public class ApplicationStarter {

    public static void startApplication() {
        WordNetWrapper.generateDictionary();
    }

}
