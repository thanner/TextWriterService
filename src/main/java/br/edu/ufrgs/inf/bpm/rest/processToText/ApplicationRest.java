package br.edu.ufrgs.inf.bpm.rest.processToText;

import br.edu.ufrgs.inf.bpm.bpmn.TDefinitions;
import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.ProcessModelBuilder;
import net.didion.jwnl.JWNLException;
import processToText.dataModel.process.ProcessModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
            TDefinitions definitions = JaxbWrapper.convertXMLToObject(bpmnString);

            ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
            ProcessModel processModel = processModelBuilder.buildProcess(definitions);
            process = TextGenerator.generateText(processModel, 0);
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(process).build();
    }

}