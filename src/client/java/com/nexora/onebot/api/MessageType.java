package com.nexora.onebot.api;

import com.google.gson.annotations.SerializedName;

public enum MessageType {
    @SerializedName("private")
    PRIVATE,
    @SerializedName("group")
    GROUP
}
