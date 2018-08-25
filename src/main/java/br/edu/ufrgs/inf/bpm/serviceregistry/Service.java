package br.edu.ufrgs.inf.bpm.serviceregistry;

import br.edu.ufrgs.inf.bpm.builder.TextGenerator;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import net.didion.jwnl.JWNLException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;

@RestController
public class Service {

    @RequestMapping(value = "/hasConnected", method = RequestMethod.GET)
    public boolean hasConnected() {
        return true;
    }

    @RequestMapping(value = "/getText", method = RequestMethod.POST)
    public Text getText(@RequestBody String bpmnString) throws IOException, JWNLException {
        return TextGenerator.generateText(bpmnString);
    }

    // ELE NÃO CONSEGUE ENTENDER O MÉTODO 2
    @RequestMapping(value = "/sayHolaz", method = RequestMethod.GET)
    public String sayHolaz() {
        return "Holaz";
    }

    @GET
    @Path("/sayHola")
    public String sayHola() {
        return "Hola";
    }

}