package com.nexora.onebot;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.nexora.Mcqq;
import com.nexora.onebot.message.MessageEventMessage;
import com.nexora.onebot.meta.MetaEventMessage;

public abstract class OnebotMessage {
    @SerializedName("time")
    protected long timestamp;

    @SerializedName("self_id")
    protected long selfId;

    @SerializedName("post_type")
    protected PostType postType;

    protected static final Gson gson = new Gson();

    public PostType getPostType() {
        return postType;
    }

    public long getSelfId() {
        return selfId;
    }

    public long getTimestamp() {
        return timestamp;
    }


    public static Optional<? extends OnebotMessage> deserializeFrom(String rawJson) {
        try {
            JsonObject object = JsonParser.parseString(rawJson).getAsJsonObject();        
            
            return Optional.ofNullable(object.get("post_type"))
                .map(JsonElement::getAsString)
                .map(postType -> {
                    return switch (postType) {
                        case "meta_event" -> MetaEventMessage.deserializeFrom(object);
                        case "message" -> MessageEventMessage.deserializeFrom(object);
                        default -> {
                            Mcqq.LOGGER.error("unknown or unsupported post type: {}", postType);
                            yield Optional.<OnebotMessage>empty();
                        }
                    };
                })
                .orElse(Optional.<OnebotMessage>empty());

        } catch (JsonParseException | IllegalStateException e) {
            Mcqq.LOGGER.error("error deserializing onebot protocol message: {}", rawJson);
            return Optional.empty();
        }
    }
}
