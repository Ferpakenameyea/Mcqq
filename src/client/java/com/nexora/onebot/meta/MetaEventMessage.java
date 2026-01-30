package com.nexora.onebot.meta;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nexora.onebot.OnebotMessage;

public abstract class MetaEventMessage extends OnebotMessage {
    @SerializedName("meta_event_type")
    protected MetaEventType metaEventType;

    public static Optional<MetaEventMessage> deserializeFrom(JsonObject jsonObject) {
        Optional<String> metaEventType = Optional.ofNullable(jsonObject.get("meta_event_type"))
            .map(JsonElement::getAsString);

        return metaEventType.map(type -> {
            switch (type) {
                case "lifecycle":
                    return OnebotMessage.gson.fromJson(jsonObject, LifeCycleMessage.class);
                case "heartbeat":
                    return OnebotMessage.gson.fromJson(jsonObject, HeartBeatMessage.class);
                default:
                    return null;
            }
        });
    }
}
