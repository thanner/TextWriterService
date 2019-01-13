package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.builder.MetaTextGenerator;
import br.edu.ufrgs.inf.bpm.textmetadata.TTextMetadata;
import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.validation.Validation;
import br.edu.ufrgs.inf.bpm.validation.ValidationDataText;
import br.edu.ufrgs.inf.bpm.validation.ValidationView;
import br.edu.ufrgs.inf.bpm.wrapper.JsonWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataGeneratorApp {

    private static Logger logger = Logger.getLogger("DataGeneratorLog");

    private static List<ValidationDataText> validationDataTextList = new ArrayList<>();

    public static void main(String[] args) {
        prepareLogger();
        //getInputFiles();
        ApplicationStarter.startApplication();

        /*
        File folder = new File(Paths.LocalOthersPath + Paths.dataInputPath);
        for (File fileEntry : folder.listFiles()) {
            generateData(fileEntry, true);
        }
        */

        //generateData(new File("src/main/others/TestData/dissertation-example.bpmn"), false);
        //generateData(new File("src/main/others/TestData/gateways.bpmn"), false);


        // 2 inputs
        //generateData(new File("src/main/others/TestData/originalProcess/5.4 - Oracle Tutorial - originalProcess.bpmn"), false);
        //generateData(new File("src/main/others/TestData/originalProcess/9.4 - New Exercise 3b - originalProcess.bpmn"), false);

        // Diferente
        //generateData(new File("src/main/others/TestData/originalProcess/1.2 - Computer repair - originalProcess.bpmn"), false);

        // Gera um gateway fora do processo (erro do textreader)
        // File file = new File("src/main/others/TestData/input/10.2 - Process B3 - originalText - process.bpmn");
        // generateData(file, false);

        //generateData(new File("src/main/others/TestData/originalProcess/11.11 - Exercise 4.4 Model the following process fragment. - originalProcess.bpmn"), false);
        //generateData(new File("src/main/others/TestData/intermediate.bpmn"), false);
        generateData(new File("src/main/others/TestData/subprocessintermediate.bpmn"), false);

        // TODO: Intermediate Marked
        //File file = new File("src/main/others/TestData/input/2.1 - SLA Violation - originalText - process.bpmn");
        //generateData(file, false);

        //File file = new File("src/main/others/TestData/sese22.bpmn");
        //generateData(file, false);

        /*
        file = new File("src/main/others/TestData/xorAnd.bpmn");
        generateData(file, false);

        file = new File("src/main/others/TestData/andXor.bpmn");
        generateData(file, false);

        file = new File("src/main/others/TestData/skip.bpmn");
        generateData(file, false);

        file = new File("src/main/others/TestData/loop.bpmn");
        generateData(file, false);

        */

        // File file2 = new File("src/main/others/TestData/input/2009-2 Conduct Directions Hearing - process.bpmn");

        // File file2 = new File("src/main/others/TestData/agreg.bpmn");

        // File file2 = new File("src/main/others/TestData/input/2009-2 Conduct Directions Hearing - process.bpmn");

        //File file2 = new File("src/main/others/TestData/3.2 - Conduct Directions Hearing - originalProcess.bpmn");
        //generateData(file2, false);

        //generateData(file2, false);

        // file2 = new File("src/main/others/TestData/skip2.bpmn");
        // generateData(file2, false);

        /*
        File file2 = new File("/Users/thanner/IdeaProjects/textwriter/src/main/others/TestData/input/2009-5 PandE - Lodge Originating Document by Post - process.bpmn");
        generateData(file2, false);

        generateValidation();
        */
    }

    private static void prepareLogger() {
        try {
            FileHandler fileHandler = new FileHandler(Paths.LocalOthersPath + Paths.dataGeneratorLogPath);
            logger.addHandler(fileHandler);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getInputFiles() {
        try {
            File outFolder = new File(Paths.externalInputFiles);
            for (File fileEntry : outFolder.listFiles()) {
                if (FilenameUtils.getExtension(fileEntry.getName()).equals("bpmn")) {
                    FileUtils.copyFile(fileEntry, new File(Paths.LocalOthersPath + Paths.dataInputPath + fileEntry.getName()));
                }
            }
        } catch (Exception e) {
            logger.warning("InputFiles not loading");
        }
    }

    private static void generateData(File inputFile, boolean verifyOnlyNewFiles) {
        try {
            String inputFileName = FilenameUtils.removeExtension(inputFile.getName());
            logger.info("Input File - " + inputFileName);
            String bpmnProcess = FileUtils.readFileToString(inputFile, "UTF-8");

            String processFileName = generateOutputProcessFileName(inputFileName);
            File processFile = new File(processFileName);
            if (verifyOnlyNewFiles && processFile.exists()) {
                logger.info("MetaText already exists");
            } else {
                TTextMetadata metaText = MetaTextGenerator.generateMetaText(bpmnProcess);
                String metaTextString = JsonWrapper.getJson(metaText);
                FileUtils.writeStringToFile(processFile, metaTextString, "UTF-8");
                logger.info("MetaText - Done");

                Validation validation = new Validation();
                validationDataTextList.add(validation.getTextValidationData(inputFileName, bpmnProcess, metaText.getText()));
            }

        } catch (Exception e) {
            logger.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private static String generateOutputProcessFileName(String inputFileName) {
        String outputFileName = Paths.LocalOthersPath + Paths.dataOutputPath + inputFileName + ".json";
        return outputFileName.replace("- process", "- metatext");
    }

    private static void generateValidation() {
        ValidationView validationView = new ValidationView();
        String validation = validationView.getValidation(validationDataTextList);
        System.out.println(validation);
    }

}
