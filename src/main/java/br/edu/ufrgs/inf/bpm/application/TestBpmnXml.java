package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import net.didion.jwnl.JWNLException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestBpmnXml {

    public static void main(String[] args) {
        System.out.println(getStructuredText());
    }

    public static String getStructuredText(){
        Text metaText = new Text();
        try {
            String bpmnProcess = FileUtils.readFileToString(new File("src/main/others/apenasteste.bpmn"), "UTF-8");
            metaText = TextGenerator.generateText(bpmnProcess);
        } catch (JWNLException | IOException e) {
            e.printStackTrace();
        }

        return metaText.toString();
    }

}
