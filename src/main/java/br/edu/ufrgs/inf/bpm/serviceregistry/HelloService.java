package br.edu.ufrgs.inf.bpm.serviceregistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sayHello")
public interface HelloService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String sayHello();

}
