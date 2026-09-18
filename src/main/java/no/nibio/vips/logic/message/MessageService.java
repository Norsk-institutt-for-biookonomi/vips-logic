package no.nibio.vips.logic.message;

import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;

public interface MessageService {

    Message getMessage(Integer messageId);

    List<Message> getMessageList(Integer organizationId, Date publishedFrom, Date publishedTo);

    List<Message> getCurrentFilteredMessagesForOrganization(List<Integer> tagIds, Integer organizationId);

    List<MessageTag> getMessageTagList();
}
