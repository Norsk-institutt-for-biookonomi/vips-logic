package no.nibio.vips.logic.message;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.logic.entity.MessageTag;

@ApplicationScoped
@Path("messagetag")
public class MessageTagResource {

    private final MessageService messageService;

    @Inject
    public MessageTagResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @GET
    @Path("list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(MessageTag[].class)
    public Response getMessageTagList() {
        return Response.ok().entity(messageService.getMessageTagList()).build();
    }
}
