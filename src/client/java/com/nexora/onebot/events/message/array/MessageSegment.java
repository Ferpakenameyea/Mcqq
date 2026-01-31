package com.nexora.onebot.events.message.array;

import net.minecraft.network.chat.MutableComponent;

public class MessageSegment<T extends MessageSegmentData> {
    private MessageSegmentType type;
    private T data;

    public T getData() {
        return data;
    }

    public MessageSegmentType getType() {
        return type;
    }

    public MessageSegment(MessageSegmentType type, T data) {
        this.type = type;
        this.data = data;
    }

    public MutableComponent toComponent() {
        return data.toComponent();
    }
}
