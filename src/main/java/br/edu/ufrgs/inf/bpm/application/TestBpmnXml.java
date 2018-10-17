package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.builder.MetaTextGenerator;
import br.edu.ufrgs.inf.bpm.metatext.TMetaText;
import br.edu.ufrgs.inf.bpm.metatext.TSentence;
import br.edu.ufrgs.inf.bpm.wrapper.JsonWrapper;
import net.didion.jwnl.JWNLException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestBpmnXml {

    public static void main(String[] args) {
        getStructuredText();
        // System.out.println(getStructuredText());
    }

    public static String getStructuredText() {
        TMetaText metaText = new TMetaText();
        try {
            String bpmnProcess = FileUtils.readFileToString(new File("src/main/others/leopold.bpmn"), "UTF-8");
            // metaText = TextGenerator.generateMetaText(bpmnProcess);
            ApplicationStarter.startApplication();

            metaText = MetaTextGenerator.generateMetaText(bpmnProcess);
            String metaTextString = JsonWrapper.getJson(metaText);
            // System.out.println(metaTextString);
        } catch (JWNLException | IOException e) {
            e.printStackTrace();
        }

        for (TSentence sentence : metaText.getText().getSentenceList()) {
            System.out.println(sentence.getValue());
        }

        return metaText.toString();
    }

}
