package com.nexora.onebot.api;

public final class WebSocketRequest<T> {
    private final String action;
    private final T params;
    private final String echo;

    public WebSocketRequest(String action, T params, String echo) {
        this.action = action;
        this.params = params;
        this.echo = echo;
    }

    public String getAction() {
        return action;
    }

    public T getParams() {
        return params;
    }

    public String getEcho() {
        return echo;
    }
}
