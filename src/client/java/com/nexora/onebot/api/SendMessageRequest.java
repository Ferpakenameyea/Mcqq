package com.nexora.onebot.api;

import com.google.gson.annotations.SerializedName;

public final class SendMessageRequest {
    public static final String ACTION = "send_msg";

    @SerializedName("message_type")
    private MessageType messageType;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("group_id")
    private Long groupId;

    @SerializedName("message")
    private String message;

    @SerializedName("auto_escape")
    private boolean autoEscape;

    public void setMessage(String message) {
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

    public String getMessage() {
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
