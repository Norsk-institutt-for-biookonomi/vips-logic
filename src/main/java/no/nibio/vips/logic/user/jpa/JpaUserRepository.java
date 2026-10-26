package no.nibio.vips.logic.user.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.user.UserRepository;

@ApplicationScoped
public class JpaUserRepository implements UserRepository {

    private final EntityManager em;

    @Inject
    public JpaUserRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public UserUuid findUserUuid(UUID uuid) {
        try {
            Query q = em.createNamedQuery("UserUuid.findByUserUUID", UserUuid.class);
            q.setParameter("userUUID", uuid);
            return (UserUuid) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public void deleteUserUuid(UUID uuid) {
        try {
            UserUuid u = (UserUuid) em.createNamedQuery("UserUuid.findByUserUUID")
                    .setParameter("userUUID", uuid)
                    .getSingleResult();
            em.remove(u);
        } catch (NoResultException ex) {
        }
    }

    @Override
    public VipsLogicUser findUserById(Integer userId) {
        try {
            Query q = em.createNamedQuery("VipsLogicUser.findByUserId", VipsLogicUser.class);
            q.setParameter("userId", userId);
            return (VipsLogicUser) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public VipsLogicUser findUserByEmail(String email) throws NonUniqueResultException {
        Query q = em.createNamedQuery("VipsLogicUser.findByEmail", VipsLogicUser.class);
        q.setParameter("email", email);
        try {
            return (VipsLogicUser) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public VipsLogicUser findUserByUsernameAndAuthenticationType(String username, Integer userAuthenticationTypeId) {
        Query q = em.createNamedQuery("UserAuthentication.findByUsername");
        q.setParameter("username", username);
        List<UserAuthentication> userAuthentications = q.getResultList();
        for (UserAuthentication userAuth : userAuthentications) {
            if (userAuth.getUserAuthenticationType().getUserAuthenticationTypeId().equals(userAuthenticationTypeId)) {
                return userAuth.getVipsLogicUser();
            }
        }
        return null;
    }
}
