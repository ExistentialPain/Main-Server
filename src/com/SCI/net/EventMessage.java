package com.SCI.net;

import java.io.*;

public class EventMessage {
    public EventMessage() {
        eventHeaders = new EventHeader();
        message = "";
    }

    public static EventMessage get(InputStream rawMessage) throws IOException {
        EventMessage message = new EventMessage();
        DataInputStream inpt = new DataInputStream(rawMessage);
        int size = inpt.readInt();
        int hSize = inpt.readInt();
        byte[] headers = new byte[hSize];
        byte[] msg = new byte[size - hSize];
        inpt.readFully(headers);
        inpt.readFully(msg);
        message.message = new String(msg);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headers)));
        String header;
        while ((header = br.readLine()) != null) {
            String[] hArr = header.split(":", 2);
            message.eventHeaders.addHeader(hArr[0], (hArr[1].equals("") ? null : hArr[1]));
        }
        return message;
    }

    public EventHeader getEventHeaders() {
        return eventHeaders;
    }

    public void write(String msg) {
        message += msg;
    }

    public String getBody() {
        return message;
    }

    @Override
    public String toString() {
        eventHeaders.mSize = message.length();
        return eventHeaders.toString() + message;
    }

    private String message;
    private EventHeader eventHeaders;
}
