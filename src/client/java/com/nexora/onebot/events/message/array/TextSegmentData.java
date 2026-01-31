package com.nexora.onebot.events.message.array;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TextSegmentData extends MessageSegmentData {
    private String text;

    public String getText() {
        return text;
    }

    @Override
    public MutableComponent toComponent() {
        return Component.literal(text);
    }

    public TextSegmentData(String text) {
        this.text = text;
    }
}
