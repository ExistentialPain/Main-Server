package com.SCI.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventHeader {
    public EventHeader() {
        headers = new HashMap<>();
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buf);
        ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
        DataOutputStream tempBuffer = new DataOutputStream(intermediate);
        try {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                tempBuffer.writeBytes(header.getKey() + ":" + (header.getValue() == null ? "" : header.getValue()));
                tempBuffer.writeByte('\n');
            }
            tempBuffer.flush();
            String toAppend = intermediate.toString();
            int size = toAppend.length();
            out.writeInt(size + mSize);
            out.writeInt(size);
            out.writeBytes(toAppend);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public String get(String key) {
        return headers.get(key);
    }

    int mSize;
    private HashMap<String, String> headers;
}
