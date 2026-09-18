package no.nibio.vips.logic.message;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.util.SystemTime;

@ApplicationScoped
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Inject
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message getMessage(Integer messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public List<Message> getMessageList(Integer organizationId, Date publishedFrom, Date publishedTo) {
        Date from = publishedFrom;
        Date to = publishedTo;
        if (from == null && to == null) {
            from = SystemTime.getSystemTime();
            to = from;
        }
        Organization mainOrg = messageRepository.findOrganizationById(organizationId);
        List<Organization> organizations = messageRepository.findRelatedOrganizations(mainOrg);
        List<Message> retVal = new ArrayList<>();
        for (Organization org : organizations) {
            if (from != null && to != null) {
                retVal.addAll(messageRepository.findByOrganizationAndPublicationPeriod(
                        org.getOrganizationId(), from, to));
            } else if (from == null) {
                retVal.addAll(messageRepository.findByOrganizationAndMaxPublicationPeriod(
                        org.getOrganizationId(), to));
            } else {
                retVal.addAll(messageRepository.findByOrganizationAndMinPublicationPeriod(
                        org.getOrganizationId(), from));
            }
        }
        return retVal;
    }

    @Override
    public List<Message> getCurrentFilteredMessagesForOrganization(List<Integer> tagIds, Integer organizationId) {
        return messageRepository.findCurrentFilteredForOrganization(
                tagIds, organizationId, SystemTime.getSystemTime());
    }

    @Override
    public List<MessageTag> getMessageTagList() {
        return messageRepository.findAllTags();
    }
}
