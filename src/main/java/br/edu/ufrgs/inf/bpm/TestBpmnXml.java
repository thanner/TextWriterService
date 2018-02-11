package br.edu.ufrgs.inf.bpm;

import br.edu.ufrgs.inf.bpm.bpmn.TDefinitions;
import br.edu.ufrgs.inf.bpm.builder.ProcessModelBuilder;
import br.edu.ufrgs.inf.bpm.builder.ProcessXmlConverter;
import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.util.XmlFormat;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import net.didion.jwnl.JWNLException;
import org.apache.commons.io.FileUtils;
import processToText.dataModel.process.ProcessModel;

import java.io.File;
import java.io.IOException;

public class TestBpmnXml {

    public static void main(String[] args) {
        String process = "";
        try {
            String text = FileUtils.readFileToString(new File("src/main/others/testData/bpmnFile/diagram2.bpmn"), "UTF-8");
            TDefinitions definitions = JaxbWrapper.convertXMLToObject(text);

            ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
            ProcessModel processModel = processModelBuilder.buildProcess(definitions);
            process = TextGenerator.generateText(processModel, 0);
        } catch (JWNLException | IOException e) {
            e.printStackTrace();
        }

        System.out.println(XmlFormat.format(process));
        System.out.println(ProcessXmlConverter.convertToText(process));
    }


}
