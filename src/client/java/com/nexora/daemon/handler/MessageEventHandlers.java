package com.nexora.daemon.handler;

import java.util.List;

import com.nexora.onebot.events.message.GroupMesssageEventMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public final class MessageEventHandlers {
    // TODO: load this from config
    private static final List<Long> listeningGroups = List.of(418526587L);
    
    private MessageEventHandlers() {}

    public static void handleGroupMessage(GroupMesssageEventMessage message) {
        if (listeningGroups.contains(message.getGroupId())) {
            List<Component> literals = message.buildMinecraftMessage();
            Minecraft.getInstance().execute(() -> {
                ChatComponent chat = Minecraft.getInstance().gui.getChat();

                for (Component component : literals) {
                    chat.addMessage(component);
                }
            });
        }
    }
}
