package com.nexora.onebot.events.message.array;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class UnsupportedSegmentData extends MessageSegmentData {
    @Override
    public MutableComponent toComponent() {
        return Component.literal("[not-supported]").withStyle(ChatFormatting.GRAY);
    }
}
