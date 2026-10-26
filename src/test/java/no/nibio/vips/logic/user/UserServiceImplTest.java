package no.nibio.vips.logic.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.UserUuidPK;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBean userBean;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository, userBean);
    }

    @Test
    public void findUserByUuidReturnsUserWhenUuidIsValid() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserUuid userUuid = userUuid(uuid, 7, daysFromNow(1));
        VipsLogicUser user = new VipsLogicUser();
        when(userRepository.findUserUuid(uuid)).thenReturn(userUuid);
        when(userRepository.findUserById(7)).thenReturn(user);

        VipsLogicUser found = userService.findUserByUuid(uuid);

        assertSame(user, found);
        assertEquals(uuid, found.getUserUuid());
    }

    @Test
    public void findUserByUuidDeletesExpiredUuid() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UserUuid userUuid = userUuid(uuid, 7, daysFromNow(-1));
        when(userRepository.findUserUuid(uuid)).thenReturn(userUuid);

        assertNull(userService.findUserByUuid(uuid));
        verify(userRepository).deleteUserUuid(uuid);
    }

    @Test
    public void registerLimitedUserReturnsValidationErrors() throws Exception {
        RegisterUserResult result = userService.registerLimitedUser(
                Map.of(
                        "email", "ok@example.com",
                        "username", "",
                        "firstName", "Ann",
                        "lastName", "Bee",
                        "password", "secret"),
                null,
                "localhost");

        assertTrue(result.hasErrors());
        assertEquals(List.of("Username must be set"), result.getErrorMessages());
    }

    @Test
    public void deleteCurrentUserReturnsUnauthorizedWhenNoUser() {
        when(userBean.getUserFromUUID(request)).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        DeleteMeResult result = userService.deleteCurrentUser(false, request);

        assertEquals(DeleteMeResult.Kind.UNAUTHORIZED, result.getKind());
    }

    private static UserUuid userUuid(UUID uuid, int userId, Date expiresAt) {
        UserUuid entity = new UserUuid();
        entity.setUserUuidPK(new UserUuidPK(uuid, userId));
        entity.setExpiresAt(expiresAt);
        return entity;
    }

    private static Date daysFromNow(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
}
