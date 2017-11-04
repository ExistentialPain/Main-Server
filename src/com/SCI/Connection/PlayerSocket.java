package com.SCI.Connection;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class PlayerSocket {
    public PlayerSocket(Socket s, UUID id) {
        isOpen = !s.isClosed() && s.isConnected();
        this.id = id;
    }

    public synchronized void sendMessage(Message message) throws IOException {
        out.writeBytes(new String(message.getBytes()) + "\n");
        out.flush();
    }

    public synchronized Message getMessage() throws IOException {
        byte tmp = 0;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while (tmp != '\n') {
            tmp = in.readByte();
            if (tmp != '\n') {
                buf.write(tmp);
            }
        }
        buf.flush();
        return new Message(new ByteArrayInputStream(buf.toByteArray()));
    }

    public UUID getId() {
        return id;
    }

    public boolean isOpen() {
        return isOpen;
    }


    private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isOpen;
    private UUID id;
}
