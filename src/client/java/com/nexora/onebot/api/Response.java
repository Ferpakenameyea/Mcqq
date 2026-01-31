package com.nexora.onebot.api;

public final class Response<T> {
    private String status;
    private int retCode;
    private T data;
    private String message;
    private String wording;
    private String echo;
    private String stream;

    public T getData() {
        return data;
    }
    public String getEcho() {
        return echo;
    }
    public String getMessage() {
        return message;
    }
    public int getRetCode() {
        return retCode;
    }
    public String getStatus() {
        return status;
    }
    public String getStream() {
        return stream;
    }
    public String getWording() {
        return wording;
    }
}