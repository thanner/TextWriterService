package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.bpmn.TDefinitions;
import br.edu.ufrgs.inf.bpm.builder.ProcessModelBuilder;
import br.edu.ufrgs.inf.bpm.builder.ProcessModelRefinement;
import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import net.didion.jwnl.JWNLException;
import org.apache.commons.io.FileUtils;
import processToText.dataModel.process.ProcessModel;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestBpmnXml {

    public static void main(String[] args) {
        System.out.println(getStructuredText());
    }

    public static String getStructuredText(){
        Text text = new Text();
        try {
            String process = FileUtils.readFileToString(new File("src/main/others/testData/bpmnFile/diagram6.bpmn"), "UTF-8");
            TDefinitions definitions = JaxbWrapper.convertXMLToObject(process);

            ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
            ProcessModel processModel = processModelBuilder.buildProcess(definitions);

            ProcessModelRefinement processModelRefinement = new ProcessModelRefinement(processModel);
            processModel = processModelRefinement.refineProcessModel();

            Map<Integer, String> bpmnIdMap = processModelBuilder.getBpmnIdMap();

            text = TextGenerator.generateText(processModel, bpmnIdMap, 0);
        } catch (JWNLException | IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }

}
