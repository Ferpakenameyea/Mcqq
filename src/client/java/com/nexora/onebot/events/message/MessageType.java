package com.nexora.onebot.events.message;

import com.google.gson.annotations.SerializedName;

public enum MessageType {
    @SerializedName("private")
    PRIVATE,
    @SerializedName("group")
    GROUP,
}
