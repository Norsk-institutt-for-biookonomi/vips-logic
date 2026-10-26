package no.nibio.vips.logic.user;

import jakarta.persistence.NonUniqueResultException;
import java.util.UUID;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;

public interface UserRepository {

    UserUuid findUserUuid(UUID uuid);

    void deleteUserUuid(UUID uuid);

    VipsLogicUser findUserById(Integer userId);

    VipsLogicUser findUserByEmail(String email) throws NonUniqueResultException;

    VipsLogicUser findUserByUsernameAndAuthenticationType(String username, Integer userAuthenticationTypeId);
}
