package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Sentence;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
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

    public static String getStructuredText(){
        Text metaText = new Text();
        try {
            String bpmnProcess = FileUtils.readFileToString(new File("src/main/others/leopold.bpmn"), "UTF-8");
            // metaText = TextGenerator.generateText(bpmnProcess);
            ApplicationStarter.startApplication();

            metaText = TextGenerator.generateText(bpmnProcess);
            String metaTextString = JsonWrapper.getJson(metaText);
            // System.out.println(metaTextString);
        } catch (JWNLException | IOException e) {
            e.printStackTrace();
        }

        for (Sentence sentence : metaText.getSentenceList()) {
            System.out.println(sentence.getValue());
        }

        return metaText.toString();
    }

}
