package no.nibio.vips.logic.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserResourceTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    private UserResource userResource;
    private EvaluatePasswordResource evaluatePasswordResource;

    @BeforeEach
    public void setUp() {
        userResource = new UserResource(userService);
        evaluatePasswordResource = new EvaluatePasswordResource(userService);
    }

    @Test
    public void getVipsLogicUserByUUIDReturnsOkWhenFound() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        VipsLogicUser user = new VipsLogicUser();
        when(userService.findUserByUuid(uuid)).thenReturn(user);

        Response response = userResource.getVipsLogicUserByUUID(uuid.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(user, response.getEntity());
    }

    @Test
    public void getVipsLogicUserByUUIDReturnsNotFoundWhenMissing() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userService.findUserByUuid(uuid)).thenReturn(null);

        Response response = userResource.getVipsLogicUserByUUID(uuid.toString());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getVipsLogicUserByUUIDReturnsServerErrorForInvalidUuid() {
        Response response = userResource.getVipsLogicUserByUUID("not-a-uuid");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void deleteVipsLogicUserUUIDReturnsOk() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Response response = userResource.deleteVipsLogicUserUUID(uuid.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(userService).deleteUserUuid(uuid);
    }

    @Test
    public void registerNewLimitedUserReturnsBadRequestWhenValidationFails() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        RegisterUserResult result = new RegisterUserResult(List.of("Username must be set"));
        when(userService.registerLimitedUser(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(result);

        Response response = userResource.registerNewLimitedUser(
                "{\"email\":\"a@b.c\",\"username\":\"\",\"firstName\":\"A\",\"lastName\":\"B\",\"password\":\"x\"}",
                request);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, List<String>> body = (Map<String, List<String>>) response.getEntity();
        assertEquals(List.of("Username must be set"), body.get("errorMessages"));
    }

    @Test
    public void deleteMeReturnsUnauthorized() {
        when(userService.deleteCurrentUser(null, request))
                .thenReturn(new DeleteMeResult(DeleteMeResult.Kind.UNAUTHORIZED,
                        "You are not authorized to perform this operation"));

        Response response = userResource.deleteMe(null, request);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("You are not authorized to perform this operation", response.getEntity());
    }

    @Test
    public void evaluatePassordReturnsOkWhenValid() throws Exception {
        when(userService.isPasswordValid(org.mockito.ArgumentMatchers.eq("FooBar123"),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);

        Response response = evaluatePasswordResource.evaluatePassord("FooBar123", request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(true, response.getEntity());
    }

    @Test
    public void evaluatePassordReturnsBadRequestWhenInvalid() throws Exception {
        when(userService.isPasswordValid(org.mockito.ArgumentMatchers.eq("bad"),
                org.mockito.ArgumentMatchers.any()))
                .thenThrow(new PasswordValidationException("too short"));

        Response response = evaluatePasswordResource.evaluatePassord("bad", request);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("too short", response.getEntity());
    }
}
