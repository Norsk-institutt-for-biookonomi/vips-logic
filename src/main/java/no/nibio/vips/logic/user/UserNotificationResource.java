package no.nibio.vips.logic.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.rest.AppNotificationPrefsDto;
import no.nibio.vips.logic.entity.rest.UserDeviceDto;
import no.nibio.vips.logic.messaging.AppNotificationBean;
import no.nibio.vips.logic.messaging.UserDevice;

@ApplicationScoped
@Path("user")
public class UserNotificationResource {

    private static final String JSON = "application/json;charset=UTF-8";

    private final UserBean userBean;
    private final AppNotificationBean appNotificationBean;

    @Inject
    public UserNotificationResource(UserBean userBean, AppNotificationBean appNotificationBean) {
        this.userBean = userBean;
        this.appNotificationBean = appNotificationBean;
    }

    @GET
    @Path("notifications")
    @Produces(JSON)
    public Response getNotifications(
            @Context HttpServletRequest request,
            @Context HttpHeaders headers) {
        VipsLogicUser user = userBean.resolveCurrentUser(request, headers);
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        return Response.ok(appNotificationBean.getPrefs(user.getUserId())).build();
    }

    @PUT
    @Path("notifications")
    @Consumes(JSON)
    @Produces(JSON)
    public Response putNotifications(
            AppNotificationPrefsDto prefs,
            @Context HttpServletRequest request,
            @Context HttpHeaders headers) {
        VipsLogicUser user = userBean.resolveCurrentUser(request, headers);
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (prefs == null) {
            return Response.status(Status.BAD_REQUEST).entity("Body is required").build();
        }
        return Response.ok(appNotificationBean.storePrefs(user.getUserId(), prefs)).build();
    }

    @POST
    @Path("devices")
    @Consumes(JSON)
    @Produces(JSON)
    public Response registerDevice(
            UserDeviceDto device,
            @Context HttpServletRequest request,
            @Context HttpHeaders headers) {
        VipsLogicUser user = userBean.resolveCurrentUser(request, headers);
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        String error = validateDevice(device);
        if (error != null) {
            return Response.status(Status.BAD_REQUEST).entity(error).build();
        }
        UserDevice stored = appNotificationBean.storeDevice(user.getUserId(), device);
        UserDeviceDto response = new UserDeviceDto();
        response.setFcmToken(stored.getFcmToken());
        response.setPlatform(stored.getPlatform());
        return Response.ok(response).build();
    }

    @DELETE
    @Path("devices")
    @Consumes(JSON)
    public Response deleteDevice(
            UserDeviceDto device,
            @Context HttpServletRequest request,
            @Context HttpHeaders headers) {
        VipsLogicUser user = userBean.resolveCurrentUser(request, headers);
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (device == null || device.getFcmToken() == null || device.getFcmToken().isBlank()) {
            return Response.status(Status.BAD_REQUEST).entity("fcmToken is required").build();
        }
        boolean deleted = appNotificationBean.deleteDevice(user.getUserId(), device.getFcmToken());
        return deleted
            ? Response.noContent().build()
            : Response.status(Status.NOT_FOUND).build();
    }

    private static String validateDevice(UserDeviceDto device) {
        if (device == null) {
            return "Body is required";
        }
        if (device.getFcmToken() == null || device.getFcmToken().isBlank()) {
            return "fcmToken is required";
        }
        if (device.getPlatform() == null || device.getPlatform().isBlank()) {
            return "platform is required";
        }
        String platform = device.getPlatform().trim().toUpperCase();
        if (!"ANDROID".equals(platform) && !"IOS".equals(platform)) {
            return "platform must be ANDROID or IOS";
        }
        return null;
    }
}
