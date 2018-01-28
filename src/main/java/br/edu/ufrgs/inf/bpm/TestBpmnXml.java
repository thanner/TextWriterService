package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.rest.processToText.ApplicationRest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestBpmnXml {
    public static void main(String[] args) {
        try {
            String text = FileUtils.readFileToString(new File("src/main/resources/TestData/BpmnFile/diagram2.bpmn"));

            ApplicationRest applicationRest = new ApplicationRest();
            String newText = applicationRest.getBpmnXml(text).toString();

            System.out.println(newText);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
