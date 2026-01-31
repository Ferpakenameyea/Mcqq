package com.nexora.onebot.events.message.array;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public final class ImageSegmentData extends MessageSegmentData {
    private String file;

    @Override
    public MutableComponent toComponent() {
        return Component.literal("[Image]")
            .withStyle(style -> style.withUnderlined(true)
                .withColor(TextColor.fromRgb(0x00aaff))
                .withBold(true)
                .withClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, 
                    "/mcqq img " + file))
        );
    }
}
