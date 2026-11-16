package no.nibio.vips.logic.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.metadata.Facet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.web.forms.FormValidationException;

@ApplicationScoped
@Path("user")
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getVipsLogicUserByUUID(@PathParam("userUUID") String userUUID) {
        try {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userService.findUserByUuid(uUUID);
            if (user != null) {
                return Response.ok().entity(user).build();
            }
            return Response.status(Status.NOT_FOUND).build();
        } catch (IllegalArgumentException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response deleteVipsLogicUserUUID(@PathParam("userUUID") String userUUID) {
        try {
            UUID uUUID = UUID.fromString(userUUID);
            userService.deleteUserUuid(uUUID);
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @POST
    @Path("register")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response registerNewLimitedUser(
            String userInfoBody, @Context HttpServletRequest httpServletRequest) {
        try {
            HashMap<String, Object> userInfo = new ObjectMapper()
                    .readValue(userInfoBody, new TypeReference<HashMap<String, Object>>() {});
            RegisterUserResult result = userService.registerLimitedUser(
                    userInfo,
                    SessionLocaleUtil.getI18nBundle(httpServletRequest),
                    Globals.SERVER_NAME);
            if (result.hasErrors()) {
                Map<String, List<String>> errorMsg = Map.of("errorMessages", result.getErrorMessages());
                return Response.status(Status.BAD_REQUEST).entity(errorMsg).build();
            }
            return Response.status(Status.OK).entity(result.getUser()).build();
        } catch (FormValidationException | IOException ex) {
            return Response.status(Status.BAD_REQUEST).entity("INPUT ERROR: " + ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("deleteme")
    public Response deleteMe(
            @QueryParam("keepData") Boolean keepData,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders httpHeaders) {
        DeleteMeResult result = userService.deleteCurrentUser(keepData, httpServletRequest, httpHeaders);
        switch (result.getKind()) {
            case UNAUTHORIZED:
                return Response.status(Status.UNAUTHORIZED).entity(result.getMessage()).build();
            case BAD_REQUEST:
                return Response.status(Status.BAD_REQUEST).entity(result.getMessage()).build();
            case NO_CONTENT:
                return Response.status(Status.NO_CONTENT).build();
            default:
                return Response.serverError().entity(result.getMessage()).build();
        }
    }
}
