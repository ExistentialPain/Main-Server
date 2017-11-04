package com.SCI.Connection;

import java.io.*;

public class Message {
    {
        in = null;
        out = null;
    }

    public Message() {
        buf = new ByteArrayOutputStream();
        out = new DataOutputStream(buf);
    }

    public Message(InputStream input) {
        in = new DataInputStream(input);
    }

    public Message(String pre) throws IOException {
        this();
        out.writeBytes(pre + " ");
    }

    public Message(byte[] pre) throws IOException {
        this(new String(pre));
    }

    public String readWord() throws IOException {
        if (in == null) {
            throw new IllegalStateException("This message is write-only.");
        }
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        byte b = 0;
        while (b != ' ') {
            b = in.readByte();
            if (b != ' ') {
                tmp.write(b);
            }
        }
        return new String(tmp.toByteArray());
    }

    public int readInt() throws IOException {
        if (in == null) {
            throw new IllegalStateException("This message is write-only.");
        }
        return in.readInt();
    }

    public float readFloat() throws IOException {
        if (in == null) {
            throw new IllegalStateException("This message is write-only.");
        }
        return in.readFloat();
    }

    public void writeWord(String word) throws IOException {
        if (out == null) {
            throw new IllegalStateException("This message is read-only.");
        }
        out.writeBytes(word + " ");
    }

    public void writeWord(byte[] word) throws IOException {
        if (out == null) {
            throw new IllegalStateException("This message is read-only.");
        }
        writeWord(new String(word));
    }

    public void writeInt(int number) throws IOException {
        if (out == null) {
            throw new IllegalStateException("This message is read-only.");
        }
        out.writeInt(number);
        out.writeByte(" ".getBytes()[0]);
    }

    public void writeFloat(float number) throws IOException {
        if (out == null) {
            throw new IllegalStateException("This message is read-only.");
        }
        out.writeFloat(number);
        out.writeByte(" ".getBytes()[0]);
    }

    public byte[] getBytes() throws IOException {
        if (out == null) {
            throw new IllegalStateException("This message is read-only.");
        }
        out.flush();
        return buf.toByteArray();
    }

    private DataInputStream in;
    private DataOutputStream out;
    private ByteArrayOutputStream buf;
}
