package com.SCI.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class QueryParser {
    private QueryParser() {

    }

    public static HashMap<String, String> parse(String queryString) throws MalformedURLException {
        System.out.println(queryString);
        HashMap<String, String> queries = new HashMap<>();
        String[] qs = queryString.split("&");
        for (String query : qs) {
            String[] halves = query.split("=");
            if (halves.length != 2) {
                throw new MalformedURLException("Querystring is malformed.");
            }
            try {
                queries.put(URLDecoder.decode(halves[0], StandardCharsets.UTF_8.name()),
                        URLDecoder.decode(halves[1], StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return queries;
    }
}
