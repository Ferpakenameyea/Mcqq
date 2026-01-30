package com.nexora.onebot.events.message;

import com.google.gson.annotations.SerializedName;

public final class PrivateMessageEventMessage extends MessageEventMessage {

    @SerializedName("sub_type")
    public SubType subType;

    @SerializedName("sender")
    private PrivateMessageSenderInfo sender;

    public SubType getSubType() {
        return subType;
    }

    public enum SubType {
        @SerializedName("friend")
        FRIEND,

        @SerializedName("group")
        GROUP,

        @SerializedName("other")
        OTHER,
    }
}
