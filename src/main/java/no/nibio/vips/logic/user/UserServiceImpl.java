package no.nibio.vips.logic.user;

import com.ibm.icu.util.ULocale;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.validator.routines.EmailValidator;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.controller.session.DeleteUserException;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.web.forms.FormValidationException;

@ApplicationScoped
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserBean userBean;

    @Inject
    public UserServiceImpl(UserRepository userRepository, UserBean userBean) {
        this.userRepository = userRepository;
        this.userBean = userBean;
    }

    @Override
    public boolean isPasswordValid(String password, ULocale errorMessageLocale) throws PasswordValidationException {
        return userBean.isPasswordValid(password, errorMessageLocale);
    }

    @Override
    public VipsLogicUser findUserByUuid(UUID uuid) {
        UserUuid uUuid = userRepository.findUserUuid(uuid);
        if (uUuid == null) {
            return null;
        }
        if (uUuid.getExpiresAt().before(new Date())) {
            userRepository.deleteUserUuid(uuid);
            return null;
        }
        VipsLogicUser user = userRepository.findUserById(uUuid.getUserUuidPK().getUserId());
        if (user != null) {
            user.setUserUuid(uuid);
        }
        return user;
    }

    @Override
    public void deleteUserUuid(UUID uuid) {
        userRepository.deleteUserUuid(uuid);
    }

    @Override
    public VipsLogicUser findUserByEmail(String email) throws NonUniqueResultException {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public VipsLogicUser findUserByUsernameAndAuthenticationType(String username, Integer userAuthenticationTypeId) {
        return userRepository.findUserByUsernameAndAuthenticationType(username, userAuthenticationTypeId);
    }

    @Override
    public RegisterUserResult registerLimitedUser(
            Map<String, Object> userInfo, ResourceBundle i18nBundle, String serverName)
            throws FormValidationException, IOException {
        List<String> errorMessages = new ArrayList<>();
        String email = ((String) userInfo.get("email")).toLowerCase();
        if (email == null || email.isBlank()) {
            errorMessages.add("Email must be set");
        } else if (!EmailValidator.getInstance().isValid(email)) {
            errorMessages.add(email + " is not a valid email address");
        } else {
            Boolean emailAlreadyInUse = false;
            try {
                VipsLogicUser foundUser = userRepository.findUserByEmail(email);
                emailAlreadyInUse = (foundUser != null);
            } catch (NonUniqueResultException ex) {
                emailAlreadyInUse = true;
            }
            if (emailAlreadyInUse) {
                errorMessages.add("Email " + email + " is already in use");
            }
        }

        String username = (String) userInfo.get("username");
        if (username == null || username.isBlank()) {
            errorMessages.add("Username must be set");
        } else {
            Boolean usernameExists = false;
            try {
                VipsLogicUser foundUser = userRepository.findUserByUsernameAndAuthenticationType(
                        username, UserAuthenticationType.TYPE_PASSWORD);
                usernameExists = (foundUser != null);
            } catch (NonUniqueResultException ex) {
                usernameExists = true;
            }
            if (usernameExists) {
                errorMessages.add("Username " + username + " already exists");
            }
        }

        String firstName = (String) userInfo.get("firstName");
        if (firstName == null || firstName.isBlank()) {
            errorMessages.add("First name must be set");
        }

        String lastName = (String) userInfo.get("lastName");
        if (lastName == null || lastName.isBlank()) {
            errorMessages.add("Last name must be set");
        }

        String password = (String) userInfo.get("password");
        if (password == null || password.isBlank()) {
            errorMessages.add("Password must be set");
        }

        if (errorMessages.size() > 0) {
            return new RegisterUserResult(errorMessages);
        }

        VipsLogicUser user = new VipsLogicUser();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());
        user.setPhoneCountryCode((String) userInfo.get("phoneCountryCode"));
        user.setPreferredLocale((String) userInfo.get("preferredLocale"));
        user.setOrganizationId(userBean.getOrganization((Integer) userInfo.get("organizationId")));
        user.setApprovalApplication("Registered in app");
        user.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
        user.setVipsLogicRoles(Set.of(userBean.getVipsLogicRole(VipsLogicRole.OBSERVER)));
        UserAuthenticationType uat = userBean.createUserAuthenticationTypeInstance(UserAuthenticationType.TYPE_PASSWORD);
        UserAuthentication ua = new UserAuthentication();
        ua.setUserAuthenticationType(uat);
        ua.setUsername(username.trim());
        ua.setPassword(userBean.getMD5EncryptedString(password.trim()));
        userBean.storeUserFirstTime(user, ua);
        userBean.sendUserEmailVerification(user, i18nBundle, serverName);
        return new RegisterUserResult(user);
    }

    @Override
    public DeleteMeResult deleteCurrentUser(Boolean keepData, HttpServletRequest request, HttpHeaders headers) {
        VipsLogicUser user = userBean.resolveCurrentUser(request, headers);
        if (user == null) {
            return new DeleteMeResult(
                    DeleteMeResult.Kind.UNAUTHORIZED, "You are not authorized to perform this operation");
        }

        if (user.getOrganizationId().getArchiveUser() != null
                && user.getOrganizationId().getArchiveUser().getUserId().equals(user.getUserId())) {
            return new DeleteMeResult(
                    DeleteMeResult.Kind.BAD_REQUEST,
                    "User is an archive user for organization "
                            + user.getOrganizationId().getOrganizationName()
                            + ". Can't delete it");
        }
        try {
            if (keepData != null && keepData) {
                VipsLogicUser archiveUser = user.getOrganizationId().getArchiveUser();
                if (archiveUser == null) {
                    return new DeleteMeResult(
                            DeleteMeResult.Kind.BAD_REQUEST,
                            "Your organization "
                                    + user.getOrganizationId().getOrganizationName()
                                    + " has not defined a default user for archiving your data. Please contact your systems administrator to fix this.");
                }
                userBean.transferUserResources(user, archiveUser);
            } else {
                userBean.deleteUserResources(user);
            }
            userBean.deleteUser(user);
            return new DeleteMeResult(DeleteMeResult.Kind.NO_CONTENT, null);
        } catch (DeleteUserException ex) {
            return new DeleteMeResult(DeleteMeResult.Kind.SERVER_ERROR, ex.getMessage());
        }
    }
}
