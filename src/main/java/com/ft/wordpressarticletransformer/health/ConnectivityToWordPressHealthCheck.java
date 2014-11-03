package com.ft.wordpressarticletransformer.health;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

import com.ft.wordpressarticletransformer.configuration.WordPressConnection;
import com.ft.wordpressarticletransformer.resources.WordPressResilientClient;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.wordpressarticletransformer.response.WordPressMostRecentPostsResponse;
import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityToWordPressHealthCheck extends AdvancedHealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityToWordPressHealthCheck.class);

	private static final String STATUS_OK = "ok";
	private static final Integer EXPECTED_COUNT = 1;

	private final String panicGuideUrl;
	private final List<WordPressConnection> wordPressConnections;
	private final WordPressResilientClient client;
	private final SystemId systemId;

	public ConnectivityToWordPressHealthCheck(final String healthCheckName, final WordPressResilientClient client, SystemId systemId,
											  String panicGuideUrl, List<WordPressConnection> wordPressConnections) {
		super(healthCheckName);
		this.client = client;
		this.systemId = systemId;
		this.panicGuideUrl = panicGuideUrl;
		this.wordPressConnections = wordPressConnections;
	}

	@Override
	protected AdvancedResult checkAdvanced() throws Exception {

		for (WordPressConnection wordPressConnection: wordPressConnections) {
			ClientResponse response = null;
			try {
				response = client.getRecentPosts(wordPressConnection);

				if (response.getStatus() == 200) {
					WordPressMostRecentPostsResponse output = response.getEntity(WordPressMostRecentPostsResponse.class);
					if(output != null){
						String status = output.getStatus();
						if (!STATUS_OK.equals(status)) {
							return AdvancedResult.error(this, "status field in response not \"" + STATUS_OK + "\", was " + status);
						}
						Integer count = output.getCount();
						if (!EXPECTED_COUNT.equals(count)) {
							return AdvancedResult.error(this, "count field in response not \"" + EXPECTED_COUNT + "\", was " + count);
						}
						continue;
					}
					return AdvancedResult.error(this, "Status code 200 was received from WordPress but content id did not match");

				} else {
					String message = String.format("Status code [%d] received when receiving content from WordPress.",
							response.getStatus());
					LOGGER.warn(message);
					return AdvancedResult.error(this, message);
				}
			} catch (Throwable e) {
				LOGGER.warn(getName() + ": " + "Exception during getting most recent content from WordPress", e);
				return AdvancedResult.error(this, e);
			} finally {
				if (response != null) {
					response.close();
				}
			}
		}
		return AdvancedResult.healthy("All is ok");
	}

	@Override
	protected int severity() {
		return 2;
	}

	@Override
	protected String businessImpact() {
		return "Publishes made in WordPress may not be able to be processed.";
	}

	@Override
	protected String technicalSummary() {
		return systemId + " is unable to transform WordPress content.";
	}

	@Override
	protected String panicGuideUrl() {
		return panicGuideUrl;
	}

	private URI getWordPressMostRecentUrl(WordPressConnection wordPressConnection) {
		return UriBuilder.fromPath(wordPressConnection.getPath())
				.scheme("http")
				.host(wordPressConnection.getHostName())
				.port(wordPressConnection.getPort())
				.build();
	}

}