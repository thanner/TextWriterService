package br.edu.ufrgs.inf.bpm.rest.textwriter;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import br.edu.ufrgs.inf.bpm.wrapper.JsonWrapper;
import net.didion.jwnl.JWNLException;

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
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getText(String bpmnString) {
        Text text;
        try {
            text = TextGenerator.generateText(bpmnString);
        } catch (JWNLException | IOException e) {
            return Response.serverError().build();
        }
        return Response.ok().entity(JsonWrapper.getJson(text)).build();
    }

}