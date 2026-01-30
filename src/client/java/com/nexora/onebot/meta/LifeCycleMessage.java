package com.nexora.onebot.meta;

import com.google.gson.annotations.SerializedName;

public final class LifeCycleMessage extends MetaEventMessage {
    @SerializedName("sub_type")
    private SubType subType;
    
    public enum SubType {
        @SerializedName("enable")
        ENABLE,
        @SerializedName("disable")
        DISABLE,
        @SerializedName("connect")
        CONNECT,
    }

    public SubType getSubType() {
        return subType;
    }
}
