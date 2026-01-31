package com.nexora.onebot.events.message.array;

import static net.minecraft.network.chat.Component.literal;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public final class ReplySegmentData extends MessageSegmentData {

    private String id;

    @Override
    public MutableComponent toComponent() {
        return literal("[REPLY] ").withStyle(ChatFormatting.GREEN);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReplySegmentData(String id) {
        this.id = id;
    }

    public ReplySegmentData(int id) {
        this.id = String.valueOf(id);
    }
    
}