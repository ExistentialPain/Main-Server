package com.SCI.net;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;

class ContextWrapper implements HttpHandler {
    public ContextWrapper() {
        methodHandlers = new HashMap<>();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Integer method = HttpMethod.fromString(httpExchange.getRequestMethod());
        if (methodHandlers.containsKey(method)) {
            methodHandlers.get(method).handle(httpExchange);
        } else {
            httpExchange.sendResponseHeaders(405, 0);
            httpExchange.getResponseBody().write("Unsupported HTTP verb.".getBytes());
            httpExchange.getResponseBody().close();
        }
    }

    public void registerMethod(Integer method, HttpHandler handler) {
        if (methodHandlers.containsKey(method)) {
            methodHandlers.remove(method);
        }
        methodHandlers.put(method, handler);
    }

    private HashMap<Integer, HttpHandler> methodHandlers;
}
