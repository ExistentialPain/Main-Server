package com.SCI.net;

public class Notification {
    public Notification(int code) {

    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getData() {
        return data;
    }

    private int code;
    private String author;
    private String data;
}
