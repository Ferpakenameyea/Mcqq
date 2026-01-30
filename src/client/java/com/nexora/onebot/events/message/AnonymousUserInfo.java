package com.nexora.onebot.events.message;

import com.google.gson.annotations.SerializedName;

public final class AnonymousUserInfo {
    @SerializedName("id")
    private long userId;

    @SerializedName("name")
    private String userName;

    @SerializedName("flag")
    private String flag;

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getFlag() {
        return flag;
    }
}
