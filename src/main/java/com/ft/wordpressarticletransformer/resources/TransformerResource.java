package com.ft.wordpressarticletransformer.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.content.model.Brand;
import com.ft.content.model.Content;
import com.ft.wordpressarticletransformer.response.WordPressResponse;
import com.ft.wordpressarticletransformer.transformer.BodyProcessingFieldTransformer;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientResponse;

@Path("/content")
public class TransformerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformerResource.class);

    private static final String CHARSET_UTF_8 = ";charset=utf-8";

	public static final String ORIGINATING_SYSTEM_FT_WORDPRESS = "http://www.ft.com/ontology/origin/TODO_USE_CORRECT_VALUE";

    private final BodyProcessingFieldTransformer bodyProcessingFieldTransformer;
	private final Brand fastFtBrand;//TODO replace with brand lookup
	
	private WordPressResilientClient wordPressResilientClient;

	public TransformerResource(BodyProcessingFieldTransformer bodyProcessingFieldTransformer, 
							   Brand fastFtBrand, WordPressResilientClient wordPressResilientClient) {
        this.bodyProcessingFieldTransformer = bodyProcessingFieldTransformer;
		this.fastFtBrand = fastFtBrand;
        this.wordPressResilientClient = wordPressResilientClient;
	}

	@GET
	@Timed
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
	public final Content getByPostId(@PathParam("uuid") String uuid, @QueryParam("url") URI requestUri, @Context HttpHeaders httpHeaders) {

	    String transactionId = TransactionIdUtils.getTransactionIdOrDie(httpHeaders, uuid, "Publish request");
	    
	    WordPressResponse wordPressResponse = doRequest(requestUri);

		if (wordPressResponse == null) {
			throw new NotFoundException();
		}

		String body = wrapBody(wordPressResponse.getPost().getContent());
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); //2014-10-21 05:45:30
		DateTime datePublished = formatter.parseDateTime(wordPressResponse.getPost().getDate());
		
		LOGGER.info("Returning content for uuid [{}].", uuid);
		
		Brand brand = getBrand(requestUri);        

		return Content.builder().withTitle(wordPressResponse.getPost().getTitle())
				.withPublishedDate(datePublished.toDate())
				.withXmlBody(tidiedUpBody(body, transactionId))
				.withByline(wordPressResponse.getPost().getAuthor().getName())
				.withContentOrigin(ORIGINATING_SYSTEM_FT_WORDPRESS, uuid.toString())
				.withBrands(new TreeSet<>(Arrays.asList(brand)))
				.withUuid(UUID.fromString(uuid)).build();

	}

	private Brand getBrand(URI requestUri) {
	    return new Brand("http://replace_with_actual_brand");
    }

    private String tidiedUpBody(String body, String transactionId) {
        try {
		    return bodyProcessingFieldTransformer.transform(body, transactionId);
        } catch (BodyProcessingException bpe) {
            LOGGER.error("Failed to transform body",bpe);
            throw ServerError.status(500).error("article has invalid body").exception(bpe);
        }
	}

	private String wrapBody(String originalBody) {
		return "<body>" + originalBody + "</body>";
	}

	private WordPressResponse doRequest(URI requestUri) {
		
		ClientResponse response = wordPressResilientClient.getContent(requestUri);

		int responseStatusCode = response.getStatus();
		int responseStatusFamily = responseStatusCode / 100;

		if (responseStatusFamily == 2) {
		    return getJsonFields(response);

		} else { //TODO - handle 404 etc
			throw ServerError.status(responseStatusCode).exception();
		}
	}

    private WordPressResponse getJsonFields(ClientResponse response) {   
        String rawOutput = response.getEntity(String.class);
        
        String json = rawOutput.substring(rawOutput.indexOf("{"));
        
        final ObjectMapper objectMapper = new ObjectMapper();
        WordPressResponse wordPressResponse = null;
        
        try {
            wordPressResponse = objectMapper.readValue(json, WordPressResponse.class);
        } catch (IOException e) {
            LOGGER.error("Failed to parse response from WordPress", e);
            throw ServerError.status(500).error("Failed to parse response from WordPress").exception(e);
        }
        

        return wordPressResponse;
    }


}
