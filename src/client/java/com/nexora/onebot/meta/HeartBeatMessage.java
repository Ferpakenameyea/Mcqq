package com.nexora.onebot.meta;

public class HeartBeatMessage extends MetaEventMessage {
    private long interval;
    private OnebotStatus status;

    public long getInterval() {
        return interval;
    }

    public OnebotStatus getStatus() {
        return status;
    }
}
