package com.nexora.onebot.events.message;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nexora.onebot.OnebotMessage;

public abstract class MessageEventMessage extends OnebotMessage {
    @SerializedName("message_type")
    protected MessageType messageType;

    @SerializedName("message_id")
    protected int messageId;

    @SerializedName("user_id")
    protected long userId;

    @SerializedName("raw_message")
    protected String rawMessage;

    public MessageType getMessageType() {
        return messageType;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public long getUserId() {
        return userId;
    }

    public static Optional<OnebotMessage> deserializeFrom(JsonObject jsonObject) {
        Optional<String> messageEventType = Optional.ofNullable(jsonObject.get("message_type"))
            .map(JsonElement::getAsString);

        return messageEventType.map(type -> switch (type) {
            case "private" -> OnebotMessage.gson.fromJson(jsonObject, PrivateMessageEventMessage.class);
            case "group" -> OnebotMessage.gson.fromJson(jsonObject, GroupMesssageEventMessage.class);
            default -> null;
        });
    }
}
