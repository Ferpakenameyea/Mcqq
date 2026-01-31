package com.nexora.onebot.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.nexora.onebot.events.message.array.MessageSegment;

public final class SendMessageRequest {
    public static final String ACTION = "send_msg";

    @SerializedName("message_type")
    private MessageType messageType;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("group_id")
    private Long groupId;

    @SerializedName("message")
    private List<MessageSegment<?>> message;

    @SerializedName("auto_escape")
    private boolean autoEscape;

    public void setMessage(List<MessageSegment<?>> message) {
        this.message = message;
    }

    public void setAutoEscape(boolean autoEscape) {
        this.autoEscape = autoEscape;
    }
    
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public List<MessageSegment<?>> getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isAutoEscape() {
        return autoEscape;
    }
}
