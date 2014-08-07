package com.ft.fastfttransformer;

import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.fastfttransformer.health.ConnectivityToClamoHealthCheck;
import com.ft.messaging.standards.message.v1.SystemId;
import com.sun.jersey.api.client.Client;
import io.dropwizard.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.fastfttransformer.configuration.FastFTTransformerConfiguration;
import com.ft.fastfttransformer.resources.TransformerResource;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class FastFTTransformerApplication extends Application<FastFTTransformerConfiguration> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FastFTTransformerApplication.class);

    public static void main(final String[] args) throws Exception {
        new FastFTTransformerApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<FastFTTransformerConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final FastFTTransformerConfiguration configuration, final Environment environment) throws Exception {
    	LOGGER.info("running with configuration: {}", configuration);
		Client client = new JerseyClientBuilder(environment).using(configuration.getClamoConnection().getJerseyClientConfiguration()).build("Health check connection to Clamo");

        environment.jersey().register(new BuildInfoResource());
		environment.jersey().register(new VersionResource());
        environment.jersey().register(new TransformerResource(client, configuration.getClamoConnection()));

		String healthCheckName = "Connectivity to Clamo";
		environment.healthChecks().register(healthCheckName,
				new ConnectivityToClamoHealthCheck(healthCheckName,
						client,
						SystemId.systemIdFromCode("fastft-transformer"), // TODO proper name
						"https://sites.google.com/a/ft.com/dynamic-publishing-team/", // TODO proper link
						configuration.getClamoConnection(),
						configuration.getClamoContentId()
				));

		environment.servlets().addFilter("Transaction ID Filter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");
    }

}