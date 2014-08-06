package com.ft.fastfttransformer;

import com.ft.api.util.buildinfo.VersionResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.fastfttransformer.configuration.FastFTTransformerConfiguration;
import com.ft.fastfttransformer.health.TransformerHealthCheck;
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
        environment.jersey().register(new BuildInfoResource());
		environment.jersey().register(new VersionResource());
        environment.jersey().register(new TransformerResource(configuration.getClamoConnection()));

        environment.healthChecks().register("My Health", new TransformerHealthCheck("replace me"));

		environment.servlets().addFilter("Transaction ID Filter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/content/*");
    }

}
