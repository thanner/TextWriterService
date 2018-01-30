package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.rest.processToText.ApplicationRest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestBpmnXml {
    public static void main(String[] args) {
        try {
            // System.out.println(System.getProperty("user.dir"));

            String text = FileUtils.readFileToString(new File("src/main/others/TestData/BpmnFile/diagram2.bpmn"), StandardCharsets.UTF_8);
            ApplicationRest applicationRest = new ApplicationRest();
            String newText = applicationRest.getBpmnXml(text).toString();
            System.out.println(newText);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
