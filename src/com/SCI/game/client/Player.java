package com.SCI.game.client;

import com.SCI.net.EventMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class Player {
    public Player(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public void attachIOSocket(Socket sock) {
        socket = sock;
        consecutiveAuthorisationFailures = 0;
    }

    public String getMessage() throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket is unconnected");
        }
        try {
            EventMessage message = EventMessage.get(socket.getInputStream());
            String id = message.getEventHeaders().get("author");
            String token = message.getEventHeaders().get("token");
            if (!Objects.equals(id, this.id) || !Objects.equals(token, this.token)) {
                if (++consecutiveAuthorisationFailures >= MAX_AUTH_FAILURES) {
                    connected = false;
                    throw new IOException("Authorisation failure");
                }
                return null;
            }
            return message.getBody();
        } catch (IOException e) {
            connected = false;
            throw e;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private String id, token;
    private Socket socket;
    private int consecutiveAuthorisationFailures;
    private boolean connected = false;
    private static final int MAX_AUTH_FAILURES = 5;
}
