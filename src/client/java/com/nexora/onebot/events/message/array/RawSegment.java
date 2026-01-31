package com.nexora.onebot.events.message.array;

import com.google.gson.JsonObject;

public final class RawSegment {
    private MessageSegmentType type;
    private JsonObject data;

    public JsonObject getData() {
        return data;
    }

    public MessageSegmentType getType() {
        return type;
    }
}
