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