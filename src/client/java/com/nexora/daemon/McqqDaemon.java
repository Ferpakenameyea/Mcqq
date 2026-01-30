package com.nexora.daemon;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nexora.Mcqq;
import com.nexora.config.McqqConfig;
import com.nexora.onebot.api.MessageType;
import com.nexora.onebot.api.SendMessageRequest;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public class McqqDaemon {
    private static final McqqDaemon instance = new McqqDaemon();

    private McqqWebsocketClient client;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // TODO: make these configurable in commands
    private MessageType sendingType = MessageType.GROUP;
    private long sendingId = 418526587;

    public static McqqDaemon getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void start() {
        isRunning.set(true);
        McqqConfig config = McqqConfig.getConfig();
        ChatComponent chat = Minecraft.getInstance().gui.getChat();
        chat.addMessage(Component.literal("Mcqq daemon is starting..."));
        if (!config.connectable()) {
            Mcqq.LOGGER.info("mcqq is not trying to connect since configuration is not complete.");
            chat.addMessage(Component.literal(
                "You haven't configured Mcqq, please go " + 
                "to the config screen to configure the mod " + 
                "and reload mcqq use /mcqq restart")
                    .withStyle(ChatFormatting.DARK_RED));
            stop();
            return;
        }

        client = McqqWebsocketClient.fromConfig(config);
        client.connect();
    }

    void stopFromRemote(String message) {
        isRunning.set(false);
        ChatComponent chat = Minecraft.getInstance().gui.getChat();
        Minecraft.getInstance().execute(() -> 
            chat.addMessage(
                Component.literal("Mcqq connection stopped, " + 
                                  "if this is not expected, there might be a " + 
                                  "connection error. server message: " + message)
                         .withStyle(ChatFormatting.YELLOW)
            )
        );
        Mcqq.LOGGER.info("mcqq deamon stopped, reason: " + message);
    }

    public void stop() {
        isRunning.set(false);
        client.close();
        Mcqq.LOGGER.info("mcqq daemon stopped");
    }

    private boolean ensureRunning() {
        if (!isRunning.get()) {
            Minecraft.getInstance().execute(() -> {
                ChatComponent chat = Minecraft.getInstance().gui.getChat();
                chat.addMessage(Component.literal("Mcqq daemon is not running, please start it first using /mcqq reload"));
            });
            return false;
        }
        return true;
    }

    public void sendMessage(String message) {
        if (!ensureRunning()) {
            return;
        }
        SendMessageRequest request = buildMessageRequest(message);
        client.sendQQMessage(request);
    }

    private SendMessageRequest buildMessageRequest(String message) {
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageType(sendingType);
        if (sendingType == MessageType.GROUP) {
            request.setGroupId(sendingId);
        } else {
            request.setUserId(sendingId);
        }
        request.setMessage(message);
        return request;
    }
}
