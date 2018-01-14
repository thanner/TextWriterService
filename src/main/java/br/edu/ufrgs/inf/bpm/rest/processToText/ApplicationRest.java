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
    @Path("/getText")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getBpmnXml(String bpmnString) throws IOException {

        // TODO: Apenas para demonstração
        bpmnString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<definitions id=\"definitions_d03c5d5d-53a6-4fdb-abbe-a8b065f1b2e3\" targetNamespace=\"http://camunda.org/examples\" xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <collaboration id=\"Collaboration-Collaboration\">\n" +
                "    <participant id=\"Participant-pa1684254535\" name=\"Pool\" processRef=\"Process-po1684254535\"/>\n" +
                "  </collaboration>\n" +
                "  <process id=\"Process-po1684254535\" name=\"Pool\">\n" +
                "    <laneSet id=\"LaneSet-l1684254535\">\n" +
                "      <lane id=\"Lane-456235541\" name=\"small company\"/>\n" +
                "      <lane id=\"Lane-849883170\" name=\"sales department\"/>\n" +
                "      <lane id=\"Lane-1380901600\" name=\"member of the sales department\"/>\n" +
                "      <lane id=\"Lane-735476548\" name=\"storehouse\"/>\n" +
                "      <lane id=\"Lane-1383209867\" name=\"engineering department\"/>\n" +
                "      <lane id=\"Lane-20422958\" name=\"sales department ships\"/>\n" +
                "    </laneSet>\n" +
                "    <task id=\"Task-1983865745\" name=\"manufacture customized bicycles\">\n" +
                "      <incoming>SequenceFlow-StartEvent-1380346281-Task-1983865745</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1983865745-Task-416099678</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-416099678\" name=\"receive an order\">\n" +
                "      <incoming>SequenceFlow-Task-1983865745-Task-416099678</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-416099678-Task-1655180266</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-1655180266\" name=\"create a new process instance\">\n" +
                "      <incoming>SequenceFlow-Task-416099678-Task-1655180266</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1655180266-ExclusiveGateway-718682659</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-1737618684\" name=\"reject the order\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-718682659-Task-1737618684</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1737618684-ExclusiveGateway-811012834</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-902313386\" name=\"accept the order\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-718682659-Task-902313386</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-902313386-ExclusiveGateway-811012834</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-68365599\" name=\"finish the process instance in the former case\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-811012834-Task-68365599</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-68365599-Task-2037283847</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-2037283847\" name=\"inform the storehouse in the latter case\">\n" +
                "      <incoming>SequenceFlow-Task-68365599-Task-2037283847</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-2037283847-Task-1302399222</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-1302399222\" name=\"inform the engineering department in the latter case\">\n" +
                "      <incoming>SequenceFlow-Task-2037283847-Task-1302399222</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1302399222-Task-1825350798</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-1825350798\" name=\"process the part list of the order\">\n" +
                "      <incoming>SequenceFlow-Task-1302399222-Task-1825350798</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1825350798-Task-2102760536</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-2102760536\" name=\"check the required quantity of part\">\n" +
                "      <incoming>SequenceFlow-Task-1825350798-Task-2102760536</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-2102760536-ExclusiveGateway-1125648904</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-2114064323\" name=\"reserve part\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-1125648904-Task-2114064323</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-2114064323-ExclusiveGateway-306573200</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-862580553\" name=\"part is back-ordered\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-1125648904-Task-862580553</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-862580553-ExclusiveGateway-306573200</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-1993538967\" name=\"repeat procedure\">\n" +
                "      <incoming>SequenceFlow-ParallelGateway-831397967-Task-1993538967</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-1993538967-ParallelGateway-218000393</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-254161866\" name=\"prepare everything for assembling the of the ordered bicycle in the meantime\">\n" +
                "      <incoming>SequenceFlow-ParallelGateway-831397967-Task-254161866</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-254161866-ParallelGateway-218000393</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-954135651\" name=\"back \">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-1528777713-Task-954135651</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-954135651-ExclusiveGateway-586354127</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-489218958\" name=\"finish \">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-586354127-Task-489218958</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-489218958-Task-888902891</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-888902891\" name=\"assemble the bicycle\">\n" +
                "      <incoming>SequenceFlow-Task-489218958-Task-888902891</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-888902891-Task-2116296342</outgoing>\n" +
                "    </task>\n" +
                "    <task id=\"Task-2116296342\" name=\"finish the process instance\">\n" +
                "      <incoming>SequenceFlow-Task-888902891-Task-2116296342</incoming>\n" +
                "      <outgoing>SequenceFlow-Task-2116296342-EndEvent-612029514</outgoing>\n" +
                "    </task>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-718682659\">\n" +
                "      <incoming>SequenceFlow-Task-1655180266-ExclusiveGateway-718682659</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-718682659-Task-1737618684</outgoing>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-718682659-Task-902313386</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-811012834\">\n" +
                "      <incoming>SequenceFlow-Task-1737618684-ExclusiveGateway-811012834</incoming>\n" +
                "      <incoming>SequenceFlow-Task-902313386-ExclusiveGateway-811012834</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-811012834-Task-68365599</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-1125648904\">\n" +
                "      <incoming>SequenceFlow-Task-2102760536-ExclusiveGateway-1125648904</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-1125648904-Task-2114064323</outgoing>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-1125648904-Task-862580553</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-306573200\">\n" +
                "      <incoming>SequenceFlow-Task-862580553-ExclusiveGateway-306573200</incoming>\n" +
                "      <incoming>SequenceFlow-Task-2114064323-ExclusiveGateway-306573200</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-306573200-ParallelGateway-831397967</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <parallelGateway id=\"ParallelGateway-831397967\">\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-306573200-ParallelGateway-831397967</incoming>\n" +
                "      <outgoing>SequenceFlow-ParallelGateway-831397967-Task-1993538967</outgoing>\n" +
                "      <outgoing>SequenceFlow-ParallelGateway-831397967-Task-254161866</outgoing>\n" +
                "    </parallelGateway>\n" +
                "    <parallelGateway id=\"ParallelGateway-218000393\">\n" +
                "      <incoming>SequenceFlow-Task-1993538967-ParallelGateway-218000393</incoming>\n" +
                "      <incoming>SequenceFlow-Task-254161866-ParallelGateway-218000393</incoming>\n" +
                "      <outgoing>SequenceFlow-ParallelGateway-218000393-ExclusiveGateway-1528777713</outgoing>\n" +
                "    </parallelGateway>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-1528777713\">\n" +
                "      <incoming>SequenceFlow-ParallelGateway-218000393-ExclusiveGateway-1528777713</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-1528777713-Task-954135651</outgoing>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-1528777713-ExclusiveGateway-586354127</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <exclusiveGateway id=\"ExclusiveGateway-586354127\">\n" +
                "      <incoming>SequenceFlow-Task-954135651-ExclusiveGateway-586354127</incoming>\n" +
                "      <incoming>SequenceFlow-ExclusiveGateway-1528777713-ExclusiveGateway-586354127</incoming>\n" +
                "      <outgoing>SequenceFlow-ExclusiveGateway-586354127-Task-489218958</outgoing>\n" +
                "    </exclusiveGateway>\n" +
                "    <startEvent id=\"StartEvent-1380346281\">\n" +
                "      <outgoing>SequenceFlow-StartEvent-1380346281-Task-1983865745</outgoing>\n" +
                "    </startEvent>\n" +
                "    <endEvent id=\"EndEvent-612029514\">\n" +
                "      <incoming>SequenceFlow-Task-2116296342-EndEvent-612029514</incoming>\n" +
                "    </endEvent>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1983865745-Task-416099678\" sourceRef=\"Task-1983865745\" targetRef=\"Task-416099678\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-416099678-Task-1655180266\" sourceRef=\"Task-416099678\" targetRef=\"Task-1655180266\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-718682659-Task-1737618684\" sourceRef=\"ExclusiveGateway-718682659\" targetRef=\"Task-1737618684\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-718682659-Task-902313386\" sourceRef=\"ExclusiveGateway-718682659\" targetRef=\"Task-902313386\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1655180266-ExclusiveGateway-718682659\" sourceRef=\"Task-1655180266\" targetRef=\"ExclusiveGateway-718682659\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1737618684-ExclusiveGateway-811012834\" sourceRef=\"Task-1737618684\" targetRef=\"ExclusiveGateway-811012834\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-902313386-ExclusiveGateway-811012834\" sourceRef=\"Task-902313386\" targetRef=\"ExclusiveGateway-811012834\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-68365599-Task-2037283847\" sourceRef=\"Task-68365599\" targetRef=\"Task-2037283847\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-2037283847-Task-1302399222\" sourceRef=\"Task-2037283847\" targetRef=\"Task-1302399222\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1302399222-Task-1825350798\" sourceRef=\"Task-1302399222\" targetRef=\"Task-1825350798\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1825350798-Task-2102760536\" sourceRef=\"Task-1825350798\" targetRef=\"Task-2102760536\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-2102760536-ExclusiveGateway-1125648904\" sourceRef=\"Task-2102760536\" targetRef=\"ExclusiveGateway-1125648904\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-862580553-ExclusiveGateway-306573200\" sourceRef=\"Task-862580553\" targetRef=\"ExclusiveGateway-306573200\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-2114064323-ExclusiveGateway-306573200\" sourceRef=\"Task-2114064323\" targetRef=\"ExclusiveGateway-306573200\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ParallelGateway-831397967-Task-1993538967\" sourceRef=\"ParallelGateway-831397967\" targetRef=\"Task-1993538967\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ParallelGateway-831397967-Task-254161866\" sourceRef=\"ParallelGateway-831397967\" targetRef=\"Task-254161866\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-1993538967-ParallelGateway-218000393\" sourceRef=\"Task-1993538967\" targetRef=\"ParallelGateway-218000393\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-254161866-ParallelGateway-218000393\" sourceRef=\"Task-254161866\" targetRef=\"ParallelGateway-218000393\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-1528777713-Task-954135651\" sourceRef=\"ExclusiveGateway-1528777713\" targetRef=\"Task-954135651\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-954135651-ExclusiveGateway-586354127\" sourceRef=\"Task-954135651\" targetRef=\"ExclusiveGateway-586354127\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-489218958-Task-888902891\" sourceRef=\"Task-489218958\" targetRef=\"Task-888902891\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-888902891-Task-2116296342\" sourceRef=\"Task-888902891\" targetRef=\"Task-2116296342\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-811012834-Task-68365599\" sourceRef=\"ExclusiveGateway-811012834\" targetRef=\"Task-68365599\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-306573200-ParallelGateway-831397967\" sourceRef=\"ExclusiveGateway-306573200\" targetRef=\"ParallelGateway-831397967\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ParallelGateway-218000393-ExclusiveGateway-1528777713\" sourceRef=\"ParallelGateway-218000393\" targetRef=\"ExclusiveGateway-1528777713\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-586354127-Task-489218958\" sourceRef=\"ExclusiveGateway-586354127\" targetRef=\"Task-489218958\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-StartEvent-1380346281-Task-1983865745\" sourceRef=\"StartEvent-1380346281\" targetRef=\"Task-1983865745\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-Task-2116296342-EndEvent-612029514\" sourceRef=\"Task-2116296342\" targetRef=\"EndEvent-612029514\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-1125648904-Task-2114064323\" name=\"the part is available \" sourceRef=\"ExclusiveGateway-1125648904\" targetRef=\"Task-2114064323\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-1125648904-Task-862580553\" name=\"part is not  available \" sourceRef=\"ExclusiveGateway-1125648904\" targetRef=\"Task-862580553\"/>\n" +
                "    <sequenceFlow id=\"SequenceFlow-ExclusiveGateway-1528777713-ExclusiveGateway-586354127\" name=\"the storehouse reserves  \" sourceRef=\"ExclusiveGateway-1528777713\" targetRef=\"ExclusiveGateway-586354127\"/>\n" +
                "  </process>\n" +
                "</definitions>\n" +
                "\n";

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

        ProcessModelXmlBuilder processModelBuilder = new ProcessModelXmlBuilder();
        ProcessModel processModel = processModelBuilder.buildProcess(bpmnString);

        String process = null;
        try {
            process = TextGenerator.generateText(processModel, 0);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return Response.status(201).entity(process).build();
    }

}