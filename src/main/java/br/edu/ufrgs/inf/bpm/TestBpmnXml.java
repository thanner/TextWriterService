package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.rest.processToText.ApplicationRest;
import org.apache.commons.io.FileUtils;
import org.camunda.bpm.model.bpmn.Bpmn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class TestBpmnXml {
    public static void main(String[] args) {
        try {
            String text = FileUtils.readFileToString(new File("src/main/others/TestData/BpmnFile/diagram3.bpmn"));

            ApplicationRest applicationRest = new ApplicationRest();
            String newText = applicationRest.getBpmnXml(text).toString();

            System.out.println(newText);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
