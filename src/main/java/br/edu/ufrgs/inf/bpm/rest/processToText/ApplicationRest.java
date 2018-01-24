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

    @POST
    @Path("/hasConnected")
    public Response hasConnected() {
        return Response.ok().build();
    }

    @POST
    @Path("/getText")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getBpmnXml(String bpmnString) throws IOException {
        String process = null;
        try {
            ProcessModelXmlBuilder processModelBuilder = new ProcessModelXmlBuilder();
            ProcessModel processModel = processModelBuilder.buildProcess(bpmnString);
            process = TextGenerator.generateText(processModel, 0);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(process).build();
    }

}