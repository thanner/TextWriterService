package br.edu.ufrgs.inf.bpm.rest.textwriter;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import net.didion.jwnl.JWNLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/application")
public class ApplicationRest {

    @POST
    @Path("/hasConnected")
    public boolean hasConnected() {
        return true;
    }

    @POST
    @Path("/getText")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Text getText(String bpmnString) throws IOException, JWNLException {
        return TextGenerator.generateText(bpmnString);
    }

}