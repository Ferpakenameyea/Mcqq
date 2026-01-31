package com.nexora.onebot.events.message;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nexora.onebot.OnebotMessage;
import com.nexora.onebot.events.message.array.ImageSegmentData;
import com.nexora.onebot.events.message.array.MessageSegment;
import com.nexora.onebot.events.message.array.MessageSegmentData;
import com.nexora.onebot.events.message.array.MessageSegmentType;
import com.nexora.onebot.events.message.array.RawSegment;
import com.nexora.onebot.events.message.array.ReplySegmentData;
import com.nexora.onebot.events.message.array.TextSegmentData;
import com.nexora.onebot.events.message.array.UnsupportedSegmentData;

public abstract class MessageEventMessage extends OnebotMessage {
    @SerializedName("message_type")
    protected MessageType messageType;

    @SerializedName("message_id")
    protected int messageId;

    @SerializedName("user_id")
    protected long userId;

    @SerializedName("raw_message")
    protected String rawMessage;

    @SerializedName("message")
    protected List<RawSegment> rawMessageArray;

    private transient List<MessageSegment<? extends MessageSegmentData>> deserializedMessages;

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

    public List<RawSegment> getRawMessageArray() {
        return rawMessageArray;
    }

    public List<MessageSegment<? extends MessageSegmentData>> getDeserializedMessages() {
        if (deserializedMessages == null) {
            deserializedMessages = deserialize(rawMessageArray);
        }

        return deserializedMessages;
    }

    private List<MessageSegment<? extends MessageSegmentData>> deserialize(List<RawSegment> rawMessageArray) {
        return rawMessageArray.stream()
            .<MessageSegment<? extends MessageSegmentData>>map(seg ->
                switch (seg.getType()) {
                    case TEXT -> new MessageSegment<>(
                        MessageSegmentType.TEXT, 
                        gson.fromJson(seg.getData(), TextSegmentData.class)
                    );
                    case IMAGE -> new MessageSegment<>(
                        MessageSegmentType.IMAGE,
                        gson.fromJson(seg.getData(), ImageSegmentData.class) 
                    );
                    case REPLY -> new MessageSegment<>(
                        MessageSegmentType.REPLY, 
                        gson.fromJson(seg.getData(), ReplySegmentData.class)
                    );
                    default -> new MessageSegment<>(
                        seg.getType(),
                        new UnsupportedSegmentData()
                    );
                }
            )
            .toList();
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
