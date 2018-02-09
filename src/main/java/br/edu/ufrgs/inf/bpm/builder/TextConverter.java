package br.edu.ufrgs.inf.bpm.builder;

public class TextConverter {

    public static String convertText(String text) {
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
