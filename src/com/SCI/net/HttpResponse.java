package com.SCI.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HttpResponse {
    public HttpResponse() {
        headers = new HashMap<>();
        fixedSize = 0;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setResponse(InputStream response) {
        this.response = response;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setFixedSize(long size) {
        fixedSize = size;
    }

    public void setResponse(String response) {
        try {
            this.response = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    HashMap<String, String> headers;
    InputStream response;
    int status;
    long fixedSize;
}
