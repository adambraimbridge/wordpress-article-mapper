package com.ft.wordpressarticletransformer.resources;

import java.net.URI;
import java.util.List;
import java.util.Set;

import com.ft.wordpressarticletransformer.model.Brand;

public class BrandSystemResolver {
    private static final String SYSTEM_ID = "http://api.ft.com/system/%s";
    private final List<BlogApiEndpointMetadata> blogApiEndpointMetadata;

    public BrandSystemResolver(List<BlogApiEndpointMetadata> blogApiEndpointMetadata) {
        this.blogApiEndpointMetadata = blogApiEndpointMetadata;
    }


    public Set<Brand> getBrand(URI requestUri) {

        if (requestUri == null || requestUri.getHost() == null) {
            return null;
        }

        String requestHostAndPath = requestUri.getHost().concat(requestUri.getPath());
        for (BlogApiEndpointMetadata hostToBrand : blogApiEndpointMetadata) {
            if (requestHostAndPath.contains(hostToBrand.getHost())) {
                return hostToBrand.getBrands();
            }
        }
        return null;
    }

    public String getOriginatingSystemId(URI requestUri) {

        if (requestUri == null || requestUri.getHost() == null) {
            return null;
        }

        for (BlogApiEndpointMetadata blogApiEndpointMetadata : this.blogApiEndpointMetadata) {
            if (requestUri.getHost().concat(requestUri.getPath()).contains(blogApiEndpointMetadata.getHost())) {
                return String.format(SYSTEM_ID, blogApiEndpointMetadata.getId());
            }
        }
        return null;
    }
}
