package br.edu.ufrgs.inf.bpm.rest.textToProcess;

import br.edu.ufrgs.inf.bpm.wrapper.ProcessModelXmlBuilder;
import org.apache.commons.io.FileUtils;
import processToText.dataModel.process.ProcessModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AppClient {

    /**
     public static void UseGet(String[] argv) {
     //String urlCallWS = "http://localhost:8080/TextToProcessRest_war_exploded/application/getResponse/BpmParser";
     //TextToProcessClient textToProcessClient = new TextToProcessClient();
     //String text = textToProcessClient.sendGet(urlCallWS, "GET");
     //System.out.println(text);

     try {
     Client client = Client.create();

     WebResource webResource = client
     .resource("http://localhost:8080/TextToProcessRest_war_exploded/application/upload");

     //String input = "{\"singer\":\"Metallica\",\"title\":\"Fade To Black\"}";

     ClientResponse response = webResource.type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, "/Users/thanner/IdeaProjects/BpmParser/src/main/resources/TestData/Text/HU/Ex1 - bycicle manufacturing.txt");

     if (response.getStatus() != 200) {
     throw new RuntimeException("Failed : HTTP error code : "
     + response.getStatus());
     }

     System.out.println("Output from Server .... \n");
     String output = response.getEntity(String.class);
     System.out.println(output);

     } catch (Exception e) {

     e.printStackTrace();

     }
     }

     public static void UsePost(String[] args) {
     TextToProcessClient textToProcessClient = new TextToProcessClient();
     String output = textToProcessClient.sendText("http://localhost:8080/TextToProcessRest_war_exploded/application/post", "123");
     System.out.println(output);
     }**/

    public static void main(String[] args) {
        try {
            TextToProcessClient textToProcessClient = new TextToProcessClient();
            File file = new File("/Users/thanner/IdeaProjects/BpmParser/src/main/resources/TestData/Text/HU/Ex1 - bycicle manufacturing.txt");
            String processBpmn = textToProcessClient.getProcess(FileUtils.readFileToString(file, StandardCharsets.UTF_8));

            ProcessModelXmlBuilder processModelXmlBuilder = new ProcessModelXmlBuilder();
            ProcessModel process = processModelXmlBuilder.buildProcess(processBpmn);

            System.out.println(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
