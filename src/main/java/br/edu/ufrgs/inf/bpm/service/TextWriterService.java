package br.edu.ufrgs.inf.bpm.service;

import br.edu.ufrgs.inf.bpm.builder.MetaTextGenerator;
import br.edu.ufrgs.inf.bpm.metatext.TMetaText;
import br.edu.ufrgs.inf.bpm.wrapper.JsonWrapper;
import io.swagger.annotations.Api;
import net.didion.jwnl.JWNLException;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;

@Api("/service")
@Service
public class TextWriterService implements ITextWriterService {

    @Override
    public Response generateText(String bpmnString) {
        try {
            TMetaText tMetaText = MetaTextGenerator.generateMetaText(bpmnString);
            return Response.ok().entity(JsonWrapper.getJson(tMetaText)).build();
        } catch (IOException | JWNLException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}