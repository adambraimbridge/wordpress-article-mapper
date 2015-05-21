package com.ft.wordpressarticletransformer.resources;

import com.ft.wordpressarticletransformer.response.WPFormat;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class UnexpectedStatusFieldException extends WordPressApiException {

    private final String status;
    private final UUID uuid;

    public UnexpectedStatusFieldException(URI requestUri, String status, UUID uuid) {
        super(String.format("Unexpected WordPress status=\"%s\" for uuid=\"%s\".", status, uuid), requestUri);
        this.status = status;
        this.uuid = uuid;
    }

    public UnexpectedStatusFieldException(URI requestUri, String status, Map<String,Object> additionalProperties) {
        super(String.format("Unexpected WordPress status=\"%s\". Expected \"%s\".", status, WPFormat.STATUS_OK), additionalProperties, requestUri);
        this.status = status;
        this.uuid = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getStatus() {
        return status;
    }

}
