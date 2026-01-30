package com.nexora.onebot.events.meta;

import com.google.gson.annotations.SerializedName;

public enum MetaEventType {
    @SerializedName("lifecycle")
    LIFE_CYCLE,
    @SerializedName("heartbeat")
    HEART_BEAT,
}
