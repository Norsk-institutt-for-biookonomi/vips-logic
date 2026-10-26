package no.nibio.vips.logic.message;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;

@ApplicationScoped
@Path("message")
public class MessageResource {

    private final MessageService messageService;

    @Inject
    public MessageResource(MessageService messageService) {
        this.messageService = messageService;
    }

    @GET
    @Path("{messageId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message.class)
    public Response getMessage(@PathParam("messageId") Integer messageId) {
        Message message = messageService.getMessage(messageId);
        if (message != null) {
            return Response.ok().entity(message).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @GET
    @Path("list/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message[].class)
    public Response getMessageList(
            @QueryParam("publishedFrom") String publishedFrom,
            @QueryParam("publishedTo") String publishedTo,
            @QueryParam("locale") String locale,
            @PathParam("organizationId") Integer organizationId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date datePublishedFrom = publishedFrom == null ? null : format.parse(publishedFrom);
            Date datePublishedTo = publishedTo == null ? null : format.parse(publishedTo);
            List<Message> messageList = messageService.getMessageList(
                    organizationId, datePublishedFrom, datePublishedTo);
            return Response.ok().entity(messageList).build();
        } catch (ParseException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("list/{organizationId}/tagfilter")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message[].class)
    public Response getMessageListWithTags(
            @QueryParam("tagId") List<Integer> tagIds,
            @PathParam("organizationId") Integer organizationId) {
        return Response.ok()
                .entity(messageService.getCurrentFilteredMessagesForOrganization(tagIds, organizationId))
                .build();
    }
}
