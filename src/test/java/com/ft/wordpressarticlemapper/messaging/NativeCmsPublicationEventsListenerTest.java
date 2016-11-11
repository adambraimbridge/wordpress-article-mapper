package com.ft.wordpressarticlemapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.wordpressarticlemapper.exception.WordPressContentException;
import com.ft.wordpressarticlemapper.response.NativeWordPressContent;
import com.ft.wordpressarticlemapper.response.Post;
import com.ft.wordpressarticlemapper.validation.NativeWordPressContentValidator;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NativeCmsPublicationEventsListenerTest {

    private static final String SYSTEM_CODE = "junit";
    private static final String TX_ID = "junittx";
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    private NativeCmsPublicationEventsListener listener;

    private NativeWordPressContent nativeWordPressContent;

    @Mock
    private MessageProducingContentMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectReader objectReader;

    @Mock
    NativeWordPressContentValidator nativeWordPressContentValidator;

    @Before
    public void setUp() throws Exception {
        listener = new NativeCmsPublicationEventsListener(mapper, objectMapper, SYSTEM_CODE, nativeWordPressContentValidator);
        nativeWordPressContent = createSampleWordpressArticle();
        when(objectReader.readValue(loadFile("messaging/native-wordpress-content.json"))).thenReturn(nativeWordPressContent);
        when(objectReader.readValue(StringUtils.EMPTY)).thenThrow(new IOException());
        when(objectMapper.reader(NativeWordPressContent.class)).thenReturn(objectReader);
    }

    @Test
    public void thatMapperIsCalledWhenMessageIsValid() throws Exception {
        Message message = getMessage();

        doNothing().when(nativeWordPressContentValidator).validateWordPressContent(any(NativeWordPressContent.class));
        assertThat(listener.onMessage(message, TX_ID), is(true));

        ArgumentCaptor<Post> c = ArgumentCaptor.forClass(Post.class);

        verify(mapper, times(1)).getWordPressArticleMessage(eq(TX_ID), c.capture(), eq(message.getMessageTimestamp()));

        Post actual = c.getValue();
        assertThat(actual, notNullValue());
        Post initial = nativeWordPressContent.getPost();
        assertThat(actual.getUuid(), equalTo(initial.getUuid()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void thatMapperIsNotCalledWhenMessageValidationFails() throws Exception {
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));

        doThrow(IllegalArgumentException.class).when(nativeWordPressContentValidator)
                .validateWordPressContent(any(NativeWordPressContent.class));

        assertThat(listener.onMessage(message, TX_ID), is(true));

        verifyZeroInteractions(mapper);
    }

    @Test
    public void thatMapperIsNotCalledWhenMessageHasNonMatchingSystemCode() {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode("foo"));
        assertThat(listener.onMessage(msg, TX_ID), is(true));
        verifyZeroInteractions(mapper);
    }

    @Test(expected = WordPressContentException.class)
    public void thatMapperThrowsExceptionWhenMessageCannotBeParsed() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        message.setMessageBody(StringUtils.EMPTY);
        listener.onMessage(message, TX_ID);
    }


    private Message getMessage() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        String messageBody = loadFile("messaging/native-wordpress-content.json");
        message.setMessageBody(messageBody != null ? messageBody : StringUtils.EMPTY);
        return message;
    }

    private NativeWordPressContent createSampleWordpressArticle() throws Exception {
        final String attributes = loadFile("messaging/native-wordpress-content.json");
        return JACKSON_MAPPER.reader(NativeWordPressContent.class).readValue(attributes);
    }

    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }
}
