package br.edu.ufrgs.inf.bpm.builder;

public class ProcessXmlConverter {

    public static String convertToText(String text) {
        return text.replace("<text>", "")
                .replace("</text>", "")
                .replace("<algo>", "")
                .replace("</algo>", "")
                .replace("<bulletpoint/>", "- ")
                .replace("<space/>", " ")
                .replace("<tab/>", "\t")
                .replace("<newline/>", "\n");
    }

}
