package br.edu.ufrgs.inf.bpm.application;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.wrapper.JsonWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DataGeneratorApp {

    private static Logger logger = Logger.getLogger("DataGeneratorLog");

    public static void main(String[] args) {
        prepareLogger();
        getInputFiles();
        ApplicationStarter.startApplication();

        File folder = new File(Paths.LocalOthersPath + Paths.dataInputPath);
        for (File fileEntry : folder.listFiles()) {
            generateData(fileEntry, true);
        }
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
                Text metaText = TextGenerator.generateText(bpmnProcess);
                String metaTextString = JsonWrapper.getJson(metaText);
                FileUtils.writeStringToFile(processFile, metaTextString, "UTF-8");
                logger.info("MetaText - Done");
            }

        } catch (Exception e) {
            logger.warning(ExceptionUtils.getStackTrace(e));
        }
    }

    private static String generateOutputProcessFileName(String inputFileName) {
        String outputFileName = Paths.LocalOthersPath + Paths.dataOutputPath + inputFileName + ".json";
        return outputFileName.replace("- process", "- metatext");
    }

}
