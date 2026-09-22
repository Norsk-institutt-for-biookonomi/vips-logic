package no.nibio.vips.logic.user.jpa;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaUserRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query query;

    @Mock
    private TypedQuery<VipsLogicUser> userQuery;

    @Mock
    private TypedQuery<UserUuid> uuidQuery;

    private JpaUserRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new JpaUserRepository(em);
    }

    @Test
    public void findUserByEmailUsesNamedQuery() {
        VipsLogicUser user = new VipsLogicUser();
        when(em.createNamedQuery("VipsLogicUser.findByEmail", VipsLogicUser.class)).thenReturn(userQuery);
        when(userQuery.setParameter("email", "a@b.c")).thenReturn(userQuery);
        when(userQuery.getSingleResult()).thenReturn(user);

        assertSame(user, repository.findUserByEmail("a@b.c"));
        verify(em).createNamedQuery("VipsLogicUser.findByEmail", VipsLogicUser.class);
    }

    @Test
    public void findUserByEmailReturnsNullWhenMissing() {
        when(em.createNamedQuery("VipsLogicUser.findByEmail", VipsLogicUser.class)).thenReturn(userQuery);
        when(userQuery.setParameter("email", "missing@b.c")).thenReturn(userQuery);
        when(userQuery.getSingleResult()).thenThrow(new NoResultException());

        assertNull(repository.findUserByEmail("missing@b.c"));
    }

    @Test
    public void findUserUuidReturnsNullWhenMissing() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(em.createNamedQuery("UserUuid.findByUserUUID", UserUuid.class)).thenReturn(uuidQuery);
        when(uuidQuery.setParameter("userUUID", uuid)).thenReturn(uuidQuery);
        when(uuidQuery.getSingleResult()).thenThrow(new NoResultException());

        assertNull(repository.findUserUuid(uuid));
    }

    @Test
    public void findUserByUsernameAndAuthenticationTypeMatchesType() {
        VipsLogicUser user = new VipsLogicUser();
        UserAuthenticationType type = new UserAuthenticationType();
        type.setUserAuthenticationTypeId(UserAuthenticationType.TYPE_PASSWORD);
        UserAuthentication auth = new UserAuthentication();
        auth.setUserAuthenticationType(type);
        auth.setVipsLogicUser(user);

        when(em.createNamedQuery("UserAuthentication.findByUsername")).thenReturn(query);
        when(query.setParameter("username", "ann")).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(auth));

        assertSame(user, repository.findUserByUsernameAndAuthenticationType("ann", UserAuthenticationType.TYPE_PASSWORD));
    }
}
