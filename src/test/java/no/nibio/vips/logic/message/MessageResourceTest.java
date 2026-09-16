package no.nibio.vips.logic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageResourceTest {

    @Mock
    private MessageService messageService;

    private MessageResource messageResource;
    private MessageTagResource messageTagResource;

    @BeforeEach
    public void setUp() {
        messageResource = new MessageResource(messageService);
        messageTagResource = new MessageTagResource(messageService);
    }

    @Test
    public void getMessageReturnsOkWhenFound() {
        Message message = new Message(12);
        when(messageService.getMessage(12)).thenReturn(message);

        Response response = messageResource.getMessage(12);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(message, response.getEntity());
    }

    @Test
    public void getMessageReturnsNotFoundWhenMissing() {
        when(messageService.getMessage(99)).thenReturn(null);

        Response response = messageResource.getMessage(99);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void getMessageListParsesDatesAndReturnsOk() {
        List<Message> messages = List.of(new Message(1));
        ArgumentCaptor<Date> fromCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> toCaptor = ArgumentCaptor.forClass(Date.class);
        when(messageService.getMessageList(org.mockito.ArgumentMatchers.eq(1), fromCaptor.capture(), toCaptor.capture()))
                .thenReturn(messages);

        Response response = messageResource.getMessageList("2021-01-01", "2021-12-31", "nb", 1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(messages, response.getEntity());
        assertEquals("2021-01-01", format(fromCaptor.getValue()));
        assertEquals("2021-12-31", format(toCaptor.getValue()));
    }

    @Test
    public void getMessageListReturnsServerErrorForInvalidDate() {
        Response response = messageResource.getMessageList("not-a-date", null, null, 1);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void getMessageListWithTagsReturnsOk() {
        List<Integer> tagIds = List.of(2, 3);
        List<Message> messages = List.of(new Message(4));
        when(messageService.getCurrentFilteredMessagesForOrganization(tagIds, 1)).thenReturn(messages);

        Response response = messageResource.getMessageListWithTags(tagIds, 1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(messages, response.getEntity());
    }

    @Test
    public void getMessageTagListReturnsOk() {
        List<MessageTag> tags = List.of(new MessageTag(1));
        when(messageService.getMessageTagList()).thenReturn(tags);

        Response response = messageTagResource.getMessageTagList();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(tags, response.getEntity());
    }

    private String format(Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
