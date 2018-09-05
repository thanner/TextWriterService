/*
package br.edu.ufrgs.inf.bpm.rest.textwriter;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.metatext.TText;
import net.didion.jwnl.JWNLException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/application")
public class ApplicationRest {

    @POST
    @Path("/hasConnected")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public boolean hasConnected() {
        return true;
    }

    @POST
    @Path("/getText")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public TText getText(@BeanParam String bpmnString) throws IOException, JWNLException {
        // return TextGenerator.generateText(bpmnString);
        return new TText();
    }

}
*/