package com.nexora.daemon.handler;

import com.nexora.onebot.meta.LifeCycleMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class MetaEventHandlers {
    private MetaEventHandlers() {}

    public static void handleLifeCycle(LifeCycleMessage message) {
        switch (message.getSubType()) {
            case ENABLE:
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().gui.getChat().addMessage(
                        Component.literal("Onebot is now enabled.").withStyle(ChatFormatting.GREEN));
                });
                break;
            case DISABLE:
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().gui.getChat().addMessage(
                        Component.literal("Onebot is now disabled.").withStyle(ChatFormatting.RED));
                });
                break;
            case CONNECT:
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().gui.getChat().addMessage(
                        Component.literal("Onebot is now connected.").withStyle(ChatFormatting.GREEN));
                });
                break;
        }
    }
}
