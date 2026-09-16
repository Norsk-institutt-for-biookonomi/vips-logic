package no.nibio.vips.logic.message.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.message.MessageRepository;

@ApplicationScoped
public class JpaMessageRepository implements MessageRepository {

    static final String CURRENT_FILTERED_SQL =
            "SELECT * FROM message WHERE message_id IN ("
                    + "SELECT message_id FROM message_message_tag "
                    + "WHERE message_tag_id IN (:tagIds) "
                    + "AND :systemTime BETWEEN date_pub AND date_valid_to "
                    + "AND organization_id IN (SELECT organization_id FROM organization WHERE organization_id=:organizationId or parent_organization_id = :organizationId) "
                    + ")";

    private final EntityManager em;

    @Inject
    public JpaMessageRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Message findById(Integer messageId) {
        return em.find(Message.class, messageId);
    }

    @Override
    public Organization findOrganizationById(Integer organizationId) {
        return em.find(Organization.class, organizationId);
    }

    @Override
    public List<Organization> findRelatedOrganizations(Organization organization) {
        return em.createNamedQuery("Organization.findAllRelated", Organization.class)
                .setParameter("organizationId", organization)
                .getResultList();
    }

    @Override
    public List<Message> findByOrganizationAndPublicationPeriod(
            Integer organizationId, Date publishedFrom, Date publishedTo) {
        return em.createNamedQuery("Message.findByOrganizationIdAndPublicationPeriod", Message.class)
                .setParameter("organizationId", organizationId)
                .setParameter("publishedFrom", publishedFrom)
                .setParameter("publishedTo", publishedTo)
                .getResultList();
    }

    @Override
    public List<Message> findByOrganizationAndMaxPublicationPeriod(Integer organizationId, Date publishedTo) {
        return em.createNamedQuery("Message.findByOrganizationIdAndMaxPublicationPeriod", Message.class)
                .setParameter("organizationId", organizationId)
                .setParameter("publishedTo", publishedTo)
                .getResultList();
    }

    @Override
    public List<Message> findByOrganizationAndMinPublicationPeriod(Integer organizationId, Date publishedFrom) {
        return em.createNamedQuery("Message.findByOrganizationIdAndMinPublicationPeriod", Message.class)
                .setParameter("organizationId", organizationId)
                .setParameter("publishedFrom", publishedFrom)
                .getResultList();
    }

    @Override
    public List<Message> findCurrentFilteredForOrganization(
            List<Integer> tagIds, Integer organizationId, Date systemTime) {
        return em.createNativeQuery(CURRENT_FILTERED_SQL, Message.class)
                .setParameter("tagIds", tagIds)
                .setParameter("systemTime", systemTime)
                .setParameter("organizationId", organizationId)
                .getResultList();
    }

    @Override
    public List<MessageTag> findAllTags() {
        return em.createNamedQuery("MessageTag.findAll", MessageTag.class).getResultList();
    }
}
