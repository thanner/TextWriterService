package br.edu.ufrgs.inf.bpm.builder;

public class ProcessXmlConverter {

    public static String convertToText(String text) {
        return text.replace("<text>", "")
                .replace("</text>", "")
                .replace("<sentence>", "")
                .replace("</sentence>", "");
    }

}
