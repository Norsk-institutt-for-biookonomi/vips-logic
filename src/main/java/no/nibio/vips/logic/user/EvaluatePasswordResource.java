package no.nibio.vips.logic.user;

import com.webcohesion.enunciate.metadata.Facet;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;

@ApplicationScoped
@Path("evaluatepassword")
public class EvaluatePasswordResource {

    private final UserService userService;

    @Inject
    public EvaluatePasswordResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Path("{password}")
    @Produces("text/plain;charset=UTF-8")
    @Facet("restricted")
    public Response evaluatePassord(
            @PathParam("password") String password,
            @Context HttpServletRequest httpServletRequest) {
        try {
            Boolean isPasswordValid = userService.isPasswordValid(
                    password, SessionLocaleUtil.getCurrentLocale(httpServletRequest));
            return Response.ok().entity(isPasswordValid).build();
        } catch (PasswordValidationException ex) {
            return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
}
