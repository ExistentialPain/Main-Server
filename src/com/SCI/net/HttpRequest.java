package com.SCI.net;

import java.net.URI;
import java.util.HashMap;

public class HttpRequest {
    HttpRequest(String body, HashMap<String, String> headers, URI uri) {
        this.body = body;
        this.headers = headers;
        this.uri = uri;
    }

    public String getBody() {
        return body;
    }

    public URI getURI() {
        return uri;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    private String body;
    private HashMap<String, String> headers;
    private URI uri;
}
