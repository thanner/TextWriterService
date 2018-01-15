package br.edu.ufrgs.inf.bpm.rest.processToText;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnXmlWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.ProcessModelXmlBuilder;
import net.didion.jwnl.JWNLException;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.*;
import processToText.dataModel.process.ProcessModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// The Java class will be hosted at the URI path "/application"
@Path("/application")
public class ApplicationRest {

    /**
     // The Java method will process HTTP POST requests
    @GET
    @Path("getResponse/{client}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public String getResponseTest(@PathParam("client") String text) {

        // Return some cliched textual content
        return JsonWrapper.getJson("TextToProcessRest sends a response to: " + text);
    }

     @POST
    //@Path("process")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String buildProcess(@FormDataParam("file") InputStream uploadedInputStream) throws IOException {
        // System.out.println(IOUtils.toString(uploadedInputStream, StandardCharsets.UTF_8));
        return "oi";

        // check if all form parameters are provided
        //if (uploadedInputStream == null || fileDetail == null)
        //    return "Invalid form data";
        // return Response.accepted().build();
        // try {
        //    return "oi"; // IOUtils.toString(uploadedInputStream, "UTF-8");
        // } catch (IOException e) {
        //    return e.getMessage().toString();
        // }

        // Return some cliched textual content
        // return JsonWrapper.getJson(text);
    }

    private static final String SERVER_UPLOAD_LOCATION_FOLDER = "/Users/thanner/IdeaProjects/TextToProcessRest";

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream) throws IOException {

        String filePath = SERVER_UPLOAD_LOCATION_FOLDER	+ "/Teste.txt";

        // save the file to the server
        // saveFile(fileInputStream, filePath);

        String output = "File saved to server location : " + filePath;

        List<String> l = new ArrayList<>();
        l.add(output);
        l.add(output);

        return Response.ok(JsonWrapper.getJson(l)).build();

    }

    // save uploaded file to a defined location on the server
    private void saveFile(InputStream uploadedInputStream,
                          String serverLocation) {

        try {
            OutputStream outpuStream;

            int read = 0;
            byte[] bytes = new byte[1024];

            outpuStream = new FileOutputStream(new File(serverLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outpuStream.write(bytes, 0, read);
            }
            outpuStream.flush();
            outpuStream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    @POST
    @Path("/post")
    @Consumes("text/plain")
    public Response getNumber(String a){
        return Response.status(201).entity("Number is: "+a.toString()).build();
    }
    **/

    //@POST
    //@Path("/getProcess")
    //@Consumes(MediaType.TEXT_PLAIN)
    //public Response getProcess(String text) throws IOException {

        // Assim eu sei que roda
        /*
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("felicidade.txt");
        StringWriter writer = new StringWriter();
        org.apache.commons.io.IOUtils.copy(is, writer, "UTF-8");
        String s = writer.toString();
        return Response.ok().entity(JsonWrapper.getJson(s)).build();
        */

        // Se quiser usar só depois que o servidor dá deploy
        // WordNetWrapper.init();
        // FrameNetWrapper.init();

    //IProcess process = ProcessGeneration.generateProcess(text);
    //return Response.status(201).entity(JsonWrapper.getJson(process)).build();
    //}

    @POST
    @Path("/hasConnected")
    public Response hasConnected() {
        return Response.ok().build();
    }

    @POST
    @Path("/getText")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getBpmnXml(String bpmnString) throws IOException {

        /**
        bpmnString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:collaboration id=\"Collaboration_0kjc3re\">\n" +
                "    <bpmn:participant id=\"Participant_1soa2uk\" name=\"Process\" processRef=\"Process_1\" />\n" +
                "  </bpmn:collaboration>\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"false\">\n" +
                "    <bpmn:laneSet>\n" +
                "      <bpmn:lane id=\"Lane_1ddm73k\" name=\"Lane A&#10;\">\n" +
                "        <bpmn:flowNodeRef>StartEvent_1</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Task_1glqzu6</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>ExclusiveGateway_1itmuo7</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Task_1kelyn5</bpmn:flowNodeRef>\n" +
                "      </bpmn:lane>\n" +
                "      <bpmn:lane id=\"Lane_1gjnt6c\" name=\"Lane B&#10;\">\n" +
                "        <bpmn:flowNodeRef>Task_13dliqk</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Task_0u5hu68</bpmn:flowNodeRef>\n" +
                "      </bpmn:lane>\n" +
                "      <bpmn:lane id=\"Lane_12u50kd\" name=\"Lane C&#10;\">\n" +
                "        <bpmn:flowNodeRef>Task_05tqmli</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>ExclusiveGateway_0abc6d2</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Task_090ccms</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>EndEvent_0x7wdqm</bpmn:flowNodeRef>\n" +
                "      </bpmn:lane>\n" +
                "    </bpmn:laneSet>\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\">\n" +
                "      <bpmn:outgoing>SequenceFlow_1s334sv</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:task id=\"Task_1glqzu6\" name=\"A1\">\n" +
                "      <bpmn:incoming>SequenceFlow_1s334sv</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_0yx4ilk</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:exclusiveGateway id=\"ExclusiveGateway_1itmuo7\">\n" +
                "      <bpmn:incoming>SequenceFlow_0yx4ilk</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_1or49yf</bpmn:outgoing>\n" +
                "      <bpmn:outgoing>SequenceFlow_0bxv6ro</bpmn:outgoing>\n" +
                "    </bpmn:exclusiveGateway>\n" +
                "    <bpmn:task id=\"Task_13dliqk\" name=\"A3\">\n" +
                "      <bpmn:incoming>SequenceFlow_1or49yf</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_144qsg6</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:task id=\"Task_1kelyn5\" name=\"A2\">\n" +
                "      <bpmn:incoming>SequenceFlow_0bxv6ro</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_03jt8zf</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:task id=\"Task_05tqmli\" name=\"A5\">\n" +
                "      <bpmn:incoming>SequenceFlow_144qsg6</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_00hkol6</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:task id=\"Task_0u5hu68\" name=\"A4\">\n" +
                "      <bpmn:incoming>SequenceFlow_03jt8zf</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_0iuq1vr</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:exclusiveGateway id=\"ExclusiveGateway_0abc6d2\">\n" +
                "      <bpmn:incoming>SequenceFlow_0iuq1vr</bpmn:incoming>\n" +
                "      <bpmn:incoming>SequenceFlow_00hkol6</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_0tnbqot</bpmn:outgoing>\n" +
                "    </bpmn:exclusiveGateway>\n" +
                "    <bpmn:task id=\"Task_090ccms\" name=\"A6\">\n" +
                "      <bpmn:incoming>SequenceFlow_0tnbqot</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_1le4jws</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:endEvent id=\"EndEvent_0x7wdqm\">\n" +
                "      <bpmn:incoming>SequenceFlow_1le4jws</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_1s334sv\" sourceRef=\"StartEvent_1\" targetRef=\"Task_1glqzu6\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0yx4ilk\" sourceRef=\"Task_1glqzu6\" targetRef=\"ExclusiveGateway_1itmuo7\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_1or49yf\" sourceRef=\"ExclusiveGateway_1itmuo7\" targetRef=\"Task_13dliqk\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0bxv6ro\" sourceRef=\"ExclusiveGateway_1itmuo7\" targetRef=\"Task_1kelyn5\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_144qsg6\" sourceRef=\"Task_13dliqk\" targetRef=\"Task_05tqmli\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_03jt8zf\" sourceRef=\"Task_1kelyn5\" targetRef=\"Task_0u5hu68\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0iuq1vr\" sourceRef=\"Task_0u5hu68\" targetRef=\"ExclusiveGateway_0abc6d2\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0tnbqot\" sourceRef=\"ExclusiveGateway_0abc6d2\" targetRef=\"Task_090ccms\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_00hkol6\" sourceRef=\"Task_05tqmli\" targetRef=\"ExclusiveGateway_0abc6d2\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_1le4jws\" sourceRef=\"Task_090ccms\" targetRef=\"EndEvent_0x7wdqm\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>\n";
        **/

        ProcessModelXmlBuilder processModelBuilder = new ProcessModelXmlBuilder();
        ProcessModel processModel = processModelBuilder.buildProcess(bpmnString);

        String process = null;
        try {
            process = TextGenerator.generateText(processModel, 0);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(process).build();
    }

}