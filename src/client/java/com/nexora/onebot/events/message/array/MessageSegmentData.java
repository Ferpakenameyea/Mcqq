package com.nexora.onebot.events.message.array;

import net.minecraft.network.chat.MutableComponent;

public abstract class MessageSegmentData {
    public abstract MutableComponent toComponent();
}