package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.WordPressBlogPostContent;
import com.ft.wordpressarticletransformer.model.WordPressContent;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.ft.wordpressarticletransformer.resources.WordPressArticleTransformerAppRule.*;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class WordPressArticleTransformerResourceTest {

    private static final Brand ALPHA_VILLE_BRAND = new Brand("http://api.ft.com/things/89d15f70-640d-11e4-9803-0800200c9a66");

    @ClassRule
    public static WordPressArticleTransformerAppRule wordPressArticleTransformerAppRule = new WordPressArticleTransformerAppRule("wordpress-article-transformer-test.yaml");

    private Client client;

    @Before
    public void setup() {
        client = Client.create();
        client.setReadTimeout(50000);
    }

    @Test
    public void shouldUnescapeHtmlNumericalEntityForTitleAndByline() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NUMBER_ENTITY);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am “London Cut”…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("<FT Labs Administrator>, <Jan Majek>, <Adam Braimbridge>")));
    }

    @Test
    public void shouldUnescapeHtmlNamedEntityForTitleAndByline() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NO_HTML_NAME_ENTITY);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The £64 million pound question & what it means for the EU…")));
        assertThat("byline", receivedContent.getByline(), is(equalTo("€FT Labs Administrator‰, £Jan Majek™, ¥Adam Braimbridge¾")));
    }

    @Test
    public void shouldReturn200AndCompleteResponseWhenContentFoundInWordPress() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

        WordPressBlogPostContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);
        assertThat("title", receivedContent.getTitle(), is(equalTo("The 6am London Cut")));
        assertThat("body", receivedContent.getBody(), containsString("<p><strong>Markets: </strong>Bourses around Asia were mixed "));
        assertThat("byline", receivedContent.getByline(), is(equalTo("FT Labs Administrator, Jan Majek, Adam Braimbridge")));
        assertThat("brands", receivedContent.getBrands(), hasItem(ALPHA_VILLE_BRAND));
        assertThat("identifier authority", receivedContent.getIdentifiers().first().getAuthority(), is(equalTo("http://api.ft.com/system/FT-LABS-WP-1-24")));
        assertThat("identifier value", receivedContent.getIdentifiers().first().getIdentifierValue(), is(equalTo("http://uat.ftalphaville.ft.com/2014/10/21/2014692/the-6am-london-cut-277/")));
        assertThat("uuid", receivedContent.getUuid(), is(equalTo(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS)));
        assertThat("comments", receivedContent.getComments().isEnabled(), is(true));
    }

    @Test
    public void inputAndOutputPublishedDateWhenFormattedShouldBeUTC() {
        String expectedOutputDate = "2014-10-21T08:45:30.000Z";
        String expectedOutputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(expectedOutputPattern);

        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_OK_SUCCESS);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        WordPressContent receivedContent = clientResponse.getEntity(WordPressBlogPostContent.class);


        assertThat("published date",
                receivedContent.getPublishedDate().toInstant().atOffset(ZoneOffset.UTC).format(fmt),
                is(expectedOutputDate));
    }

    @Test
    // this is what happens for posts that are in status=Pending, status=Draft, or visibility=Private....and deleted?
    public void shouldReturn500WithUuidWhenWordpressReturnsStatusErrorAndErrorNotFound() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_ERROR_NOT_FOUND);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response", clientResponse.getEntity(String.class), containsString("Unexpected WordPress status=\\\"error\\\""));
    }

    @Test
    public void shouldReturn404When404ReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_404);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @Test
    public void shouldReturn500WhenContentTypeNotJsonReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_INVALID_CONTENT_TYPE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response", clientResponse.getEntity(String.class), containsString("server error"));
    }

    @Test
    public void shouldReturn422WhenTypeNotPostFromWordpressResponse() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_200_NOT_TYPE_POST);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response status", clientResponse, hasProperty("status", equalTo(SC_UNPROCESSABLE_ENTITY)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("foo"));
    }

    @Test
    public void shouldReturn400WhenUuidIsNotValid() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_NO_REQUEST_TO_WORD_PRESS_EXPECTED);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response status", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn405WhenNoUuidSupplied() {
        final URI uri = buildTransformerUrlWithIdMissing();

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
    }

    @Test
    public void shouldReturn400WhenUrlIsNotValid() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORDPRESS_NO_APIURL_ON_RESPONSE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn400WhenApiUrlIsMissingFromWordpressResponse() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_NON_WORD_PRESS_RESPONSE);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    @Test
    public void shouldReturn500When500ReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_500);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
    }

    @Test
    public void shouldReturn503WhenCannotConnectToNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_CANNOT_CONNECT);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }

    @Test
    public void shouldReturn500WhenUnexpectedHttpStatusReturnedFromNativeRw() {
        final URI uri = buildTransformerUrl(UUID_MAP_TO_REQUEST_TO_WORD_PRESS_502);

        final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
        assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
        assertThat("response message", clientResponse.getEntity(String.class), containsString("Unexpected error status from Native Reader: [502]."));
    }

    @After
    public void reset() {
        WireMock.resetToDefault();
    }

    private URI buildTransformerUrl(String uuid) {
        return UriBuilder
                .fromPath("content")
                .path("{uuid}")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build(uuid);
    }

    private URI buildTransformerUrlWithIdMissing() {
        return UriBuilder
                .fromPath("content")
                .scheme("http")
                .host("localhost")
                .port(wordPressArticleTransformerAppRule.getWordPressArticleTransformerLocalPort())
                .build();
    }

}
