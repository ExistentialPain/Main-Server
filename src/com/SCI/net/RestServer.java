package com.SCI.net;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestServer {
    public RestServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        handlers = new HashMap<>();
    }

    public RestServer() throws IOException {
        this(80);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(10);
    }

    public void stop(int delay) {
        server.stop(delay);
    }

    public void registerMethodHandlerl(String path, Integer method, HttpHandler handler) {
        if (handlers.containsKey(path)) {
            handlers.get(path).registerMethod(method, handler);
        } else {
            ContextWrapper contextWrapper = new ContextWrapper();
            contextWrapper.registerMethod(method, handler);
            handlers.put(path, contextWrapper);
            server.createContext(path, contextWrapper);
        }
    }

    public void registerMethodHandler(String path, Integer method, RequestHandler handler) {
        registerMethodHandlerl(path, method, httpExchange -> {
            Headers headers = httpExchange.getRequestHeaders();
            HashMap<String, String> heads = new HashMap<>();
            String body = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody())).lines().collect(Collectors.joining("\n"));
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                heads.put(header.getKey(), header.getValue().get(header.getValue().size() - 1));
            }
            HttpResponse response;
            try {
                response = handler.handle(new HttpRequest(body, heads, httpExchange.getRequestURI()));
                if (response == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                response = new HttpResponse();
                response.setStatus(500);
                response.setResponse("An internal server error occurred.");
                e.printStackTrace();
            }
            for (Map.Entry<String, String> header : response.headers.entrySet()) {
                httpExchange.getResponseHeaders().add(header.getKey(), header.getValue());
            }
            httpExchange.sendResponseHeaders(response.status, response.fixedSize);

            byte[] buffer = new byte[bufSize];
            int read;
            while ((read = response.response.read(buffer)) > -1) {
                httpExchange.getResponseBody().write(buffer, 0, read);
            }
            httpExchange.getResponseBody().close();
        });
    }

    private HttpServer server;
    private HashMap<String, ContextWrapper> handlers;
    public static void setBufferSize(int size) {
        bufSize = size;
    }
    private static int bufSize = 1024;
}
