package no.nibio.vips.logic.message.jpa;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaMessageRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query nativeQuery;

    @Mock
    private TypedQuery<Message> messageQuery;

    @Mock
    private TypedQuery<Organization> organizationQuery;

    @Mock
    private TypedQuery<MessageTag> messageTagQuery;

    private JpaMessageRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new JpaMessageRepository(em);
    }

    @Test
    public void findByIdUsesEntityManager() {
        Message message = new Message(12);
        when(em.find(Message.class, 12)).thenReturn(message);

        assertSame(message, repository.findById(12));
    }

    @Test
    public void findRelatedOrganizationsUsesNamedQuery() {
        Organization organization = new Organization();
        List<Organization> related = List.of(organization);
        when(em.createNamedQuery("Organization.findAllRelated", Organization.class)).thenReturn(organizationQuery);
        when(organizationQuery.setParameter("organizationId", organization)).thenReturn(organizationQuery);
        when(organizationQuery.getResultList()).thenReturn(related);

        assertSame(related, repository.findRelatedOrganizations(organization));
        verify(organizationQuery).setParameter("organizationId", organization);
    }

    @Test
    public void findByOrganizationAndPublicationPeriodUsesNamedQuery() {
        Date from = new Date(1L);
        Date to = new Date(2L);
        List<Message> messages = List.of(new Message(1));
        when(em.createNamedQuery("Message.findByOrganizationIdAndPublicationPeriod", Message.class))
                .thenReturn(messageQuery);
        when(messageQuery.setParameter("organizationId", 1)).thenReturn(messageQuery);
        when(messageQuery.setParameter("publishedFrom", from)).thenReturn(messageQuery);
        when(messageQuery.setParameter("publishedTo", to)).thenReturn(messageQuery);
        when(messageQuery.getResultList()).thenReturn(messages);

        assertSame(messages, repository.findByOrganizationAndPublicationPeriod(1, from, to));
    }

    @Test
    public void findCurrentFilteredForOrganizationUsesNativeQuery() {
        Date systemTime = new Date(3L);
        List<Integer> tagIds = List.of(2);
        List<Message> messages = List.of(new Message(9));
        when(em.createNativeQuery(JpaMessageRepository.CURRENT_FILTERED_SQL, Message.class)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("tagIds", tagIds)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("systemTime", systemTime)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("organizationId", 1)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(messages);

        assertSame(messages, repository.findCurrentFilteredForOrganization(tagIds, 1, systemTime));
        verify(nativeQuery).setParameter("tagIds", tagIds);
        verify(nativeQuery).setParameter("systemTime", systemTime);
        verify(nativeQuery).setParameter("organizationId", 1);
    }

    @Test
    public void findAllTagsUsesNamedQuery() {
        List<MessageTag> tags = List.of(new MessageTag(1));
        when(em.createNamedQuery("MessageTag.findAll", MessageTag.class)).thenReturn(messageTagQuery);
        when(messageTagQuery.getResultList()).thenReturn(tags);

        assertSame(tags, repository.findAllTags());
    }
}
