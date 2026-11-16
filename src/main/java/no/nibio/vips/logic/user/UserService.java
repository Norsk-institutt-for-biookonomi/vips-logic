package no.nibio.vips.logic.user;

import com.ibm.icu.util.ULocale;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.web.forms.FormValidationException;

public interface UserService {

    boolean isPasswordValid(String password, ULocale errorMessageLocale) throws PasswordValidationException;

    VipsLogicUser findUserByUuid(UUID uuid);

    void deleteUserUuid(UUID uuid);

    VipsLogicUser findUserByEmail(String email) throws NonUniqueResultException;

    VipsLogicUser findUserByUsernameAndAuthenticationType(String username, Integer userAuthenticationTypeId);

    RegisterUserResult registerLimitedUser(
            Map<String, Object> userInfo, ResourceBundle i18nBundle, String serverName)
            throws FormValidationException, IOException;

    DeleteMeResult deleteCurrentUser(Boolean keepData, HttpServletRequest request, HttpHeaders headers);
}
