package com.nexora.onebot.events.message.array;

import com.google.gson.annotations.SerializedName;

public enum MessageSegmentType {
    @SerializedName("text")
    TEXT,
    @SerializedName("face")
    FACE,
    @SerializedName("image")
    IMAGE,
    @SerializedName("record")
    RECORD,
    @SerializedName("video")
    VIDEO,
    @SerializedName("rps")
    RPS,
    @SerializedName("dice")
    DICE,
    @SerializedName("shake")
    SHAKE,
    @SerializedName("poke")
    POKE,
    @SerializedName("anonymous")
    SHARE,
    @SerializedName("at")
    CONTACT,
    @SerializedName("location")
    LOCATION,
    @SerializedName("music")
    MUSIC,
    @SerializedName("reply")
    REPLY,
    @SerializedName("xml")
    FORWARD,
    @SerializedName("node")
    NODE,
    @SerializedName("json")
    JSON,
    @SerializedName("mface")
    MFACE,
    @SerializedName("file")
    FILE,
    @SerializedName("markdown")
    MARKDOWN,
    @SerializedName("lightapp")
    LIGHTAPP
}
