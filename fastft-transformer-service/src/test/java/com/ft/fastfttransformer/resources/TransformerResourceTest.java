package com.ft.fastfttransformer.resources;

import com.ft.content.model.Content;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

public class TransformerResourceTest {

	@ClassRule
	public static FastFtTransformerAppRule fastFtTransformerAppRule = new FastFtTransformerAppRule("fastft-transformer-test.yaml");

	private static final int SAMPLE_CONTENT_ID = 186672;
	private static final int WILL_RETURN_404 = 186673;
	private static final int WILL_RETURN_503 = 186674;
	private static final int WILL_RETURN_500 = 186675;
	private static final int WILL_RETURN_CANT_CONNECT = 186676;
	private static final int WILL_RETURN_200_NOT_FOUND = 186672999;

	@Test
	public void shouldReturn200AndCompleteResponseWhenContentFoundInClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		final URI uri = buildTransformerUrl(SAMPLE_CONTENT_ID);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

		Content receivedContent = clientResponse.getEntity(Content.class);
		assertThat("title", receivedContent.getTitle(), is(equalTo("US durable goods jump in June")));
		assertThat("body", receivedContent.getBody(), is(equalTo(EXPECTED_BODY)));
		assertThat("byline", receivedContent.getByline(), is(equalTo("By FastFT")));
		assertThat("source", receivedContent.getSource(), is(equalTo("FT")));
		assertThat("uuid", receivedContent.getUuid(), is(equalTo("ca93067c-6b1d-3b6f-bd54-f4cd5598961a")));
		assertThat("published date", receivedContent.getPublishedDate(), is(new Date(1406291632000L)));
	}

	@Test
	public void shouldReturn503When404ReturnedFromClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		final URI uri = buildTransformerUrl(WILL_RETURN_404);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}

	@Test
	public void shouldReturn503When503ReturnedFromClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		final URI uri = buildTransformerUrl(WILL_RETURN_503);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
	}

	@Test
	public void shouldReturn500When500ReturnedFromClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		final URI uri = buildTransformerUrl(WILL_RETURN_500);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}

	@Test
	public void shouldReturn503WhenCannotConnectToClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		WireMock.stubFor(WireMock.get(WireMock.urlMatching("/api/186676.*")).willReturn(WireMock.aResponse().withFault(Fault.EMPTY_RESPONSE)));
		final URI uri = buildTransformerUrl(WILL_RETURN_CANT_CONNECT);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
	}

	@Test
	public void shouldReturn404WhenContentNotFoundInClamo() {
		final Client client = Client.create();
		client.setReadTimeout(5000);
		final URI uri = buildTransformerUrl(WILL_RETURN_200_NOT_FOUND);

		final ClientResponse clientResponse = client.resource(uri).get(ClientResponse.class);
		assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
	}

	private URI buildTransformerUrl(int contentId) {
		return UriBuilder
				.fromPath("content")
				.path("{contentId}")
				.scheme("http")
				.host("localhost")
				.port(fastFtTransformerAppRule.getFastFtTransformerLocalPort())
				.build(contentId);
	}
	private final static String EXPECTED_BODY = "<body>The question of why corporate America isn't investing much has become one of the most vexed as everyone scours for a potential catalyst to unlock faster economic growth.<!--more-->\n" +
			"\n" +
			"It's why the monthly report from the Commerce Department on durable goods, which are taken as a proxy for business spending, garners some attention from investors.\n" +
			"\n" +
			"The [latest figures](http://www.census.gov/manufacturing/m3/adv/pdf/durgd.pdf) for June show durable goods climbed 0.7 per cent to $239.9bn, up from a 1 per cent decline in May and better than the 0.5 per cent rise forecast.\n" +
			"\n" +
			"Of more interest, a measure of orders excluding aircraft and defence orders rose 1.4 per cent, snapping two months of declines. Economists had predicted a 1.3 per cent jump.\n" +
			"\n" +
			"Companies' orders for machinery drove the overall rise. Machinery orders climbed 2.4 per cent to $37.7bn. \n" +
			"\n" +
			"June's overall increase is at least further confirmation that the economy rebounded last quarter from its contraction at the start of the year. \n" +
			"\n" +
			"\n" +
			"\n" +
			" \n" +
			"\n" +
			"\n" +
			"\n</body>";

}
