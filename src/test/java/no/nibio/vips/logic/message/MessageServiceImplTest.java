package no.nibio.vips.logic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    private MessageServiceImpl messageService;

    @BeforeEach
    public void setUp() {
        messageService = new MessageServiceImpl(messageRepository);
    }

    @Test
    public void getMessageDelegatesToRepository() {
        Message message = new Message(8);
        when(messageRepository.findById(8)).thenReturn(message);

        assertSame(message, messageService.getMessage(8));
    }

    @Test
    public void getMessageListUsesPublicationPeriodWhenBothDatesSet() {
        Organization org = organization(1);
        Date from = new Date(1_000L);
        Date to = new Date(2_000L);
        List<Message> messages = List.of(new Message(3));
        when(messageRepository.findOrganizationById(1)).thenReturn(org);
        when(messageRepository.findRelatedOrganizations(org)).thenReturn(List.of(org));
        when(messageRepository.findByOrganizationAndPublicationPeriod(1, from, to)).thenReturn(messages);

        assertEquals(messages, messageService.getMessageList(1, from, to));
    }

    @Test
    public void getMessageListUsesMaxPeriodWhenFromIsNull() {
        Organization org = organization(1);
        Date to = new Date(2_000L);
        List<Message> messages = List.of(new Message(4));
        when(messageRepository.findOrganizationById(1)).thenReturn(org);
        when(messageRepository.findRelatedOrganizations(org)).thenReturn(List.of(org));
        when(messageRepository.findByOrganizationAndMaxPublicationPeriod(1, to)).thenReturn(messages);

        assertEquals(messages, messageService.getMessageList(1, null, to));
    }

    @Test
    public void getMessageListUsesMinPeriodWhenToIsNull() {
        Organization org = organization(1);
        Date from = new Date(1_000L);
        List<Message> messages = List.of(new Message(5));
        when(messageRepository.findOrganizationById(1)).thenReturn(org);
        when(messageRepository.findRelatedOrganizations(org)).thenReturn(List.of(org));
        when(messageRepository.findByOrganizationAndMinPublicationPeriod(1, from)).thenReturn(messages);

        assertEquals(messages, messageService.getMessageList(1, from, null));
    }

    @Test
    public void getMessageListDefaultsBothDatesToSystemTime() {
        Organization org = organization(1);
        List<Message> messages = List.of(new Message(6));
        when(messageRepository.findOrganizationById(1)).thenReturn(org);
        when(messageRepository.findRelatedOrganizations(org)).thenReturn(List.of(org));
        when(messageRepository.findByOrganizationAndPublicationPeriod(eq(1), any(Date.class), any(Date.class)))
                .thenReturn(messages);

        assertEquals(messages, messageService.getMessageList(1, null, null));
        verify(messageRepository).findByOrganizationAndPublicationPeriod(eq(1), any(Date.class), any(Date.class));
    }

    @Test
    public void getCurrentFilteredMessagesPassesSystemTime() {
        List<Integer> tagIds = List.of(2);
        List<Message> messages = List.of(new Message(7));
        when(messageRepository.findCurrentFilteredForOrganization(eq(tagIds), eq(1), any(Date.class)))
                .thenReturn(messages);

        assertSame(messages, messageService.getCurrentFilteredMessagesForOrganization(tagIds, 1));
        verify(messageRepository).findCurrentFilteredForOrganization(eq(tagIds), eq(1), any(Date.class));
    }

    @Test
    public void getMessageTagListDelegatesToRepository() {
        List<MessageTag> tags = List.of(new MessageTag(1));
        when(messageRepository.findAllTags()).thenReturn(tags);

        assertSame(tags, messageService.getMessageTagList());
    }

    private Organization organization(Integer id) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        return organization;
    }
}
