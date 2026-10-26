package no.nibio.vips.logic.message;

import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Organization;

public interface MessageRepository {

    Message findById(Integer messageId);

    Organization findOrganizationById(Integer organizationId);

    List<Organization> findRelatedOrganizations(Organization organization);

    List<Message> findByOrganizationAndPublicationPeriod(
            Integer organizationId, Date publishedFrom, Date publishedTo);

    List<Message> findByOrganizationAndMaxPublicationPeriod(Integer organizationId, Date publishedTo);

    List<Message> findByOrganizationAndMinPublicationPeriod(Integer organizationId, Date publishedFrom);

    List<Message> findCurrentFilteredForOrganization(
            List<Integer> tagIds, Integer organizationId, Date systemTime);

    List<MessageTag> findAllTags();
}
