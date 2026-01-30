package com.nexora.daemon;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;
import com.nexora.Mcqq;
import com.nexora.config.McqqConfig;
import com.nexora.daemon.handler.MessageEventHandlers;
import com.nexora.daemon.handler.MetaEventHandlers;
import com.nexora.onebot.OnebotMessage;
import com.nexora.onebot.api.SendMessageRequest;
import com.nexora.onebot.api.WebSocketRequest;
import com.nexora.onebot.message.GroupMesssageEventMessage;
import com.nexora.onebot.meta.LifeCycleMessage;

public class McqqWebsocketClient extends WebSocketClient {

    private static final Gson gson = new Gson();

    private final Map<Class<? extends OnebotMessage>, Consumer<OnebotMessage>> handlerMap;

    public McqqWebsocketClient(URI serverUri, Map<String, String> headerMap) {
        super(serverUri, headerMap);

        Map<Class<? extends OnebotMessage>, Consumer<OnebotMessage>> handlerMap = new HashMap<>();
        
        handlerMap.put(LifeCycleMessage.class, message -> MetaEventHandlers.handleLifeCycle((LifeCycleMessage)message));
        handlerMap.put(GroupMesssageEventMessage.class, message -> MessageEventHandlers.handleGroupMessage((GroupMesssageEventMessage)message));
        
        this.handlerMap = Collections.unmodifiableMap(handlerMap);
    }

    public static McqqWebsocketClient fromConfig(McqqConfig config) {
        if (!config.connectable()) {
            Mcqq.LOGGER.error("passing a non-connectable configuration to McqqWebsocketClient initialization");
            return null;
        }

        return new McqqWebsocketClient(
            URI.create(config.getWebsocketUrl().get()), 
            Map.of("Authorization", "Bearer " + config.getNapcatToken().get()));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {}

    @Override
    public void onMessage(String message) {
        OnebotMessage.deserializeFrom(message)
            .ifPresent(this::react);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        String displayMessage = getReasonDisplayString(reason);
        
        Mcqq.LOGGER.info("websocket close code: {}, reason: {}",
            code,
            displayMessage
        );

        if (remote) {
            McqqDaemon.getInstance().stopFromRemote(displayMessage);
        }
    }

    @Override
    public void onError(Exception ex) {
        Mcqq.LOGGER.error("websocket error", ex);
    }

    private static String getReasonDisplayString(String reason) {
        if (reason == null || reason.isBlank() || reason.isEmpty()) {
            return "none";
        }

        return reason;
    }

    private void react(OnebotMessage message) {
        Mcqq.LOGGER.info("received message ({})", message.getPostType());
        Consumer<OnebotMessage> handler = handlerMap.get(message.getClass());
        if (handler != null) {
            try {
                handler.accept(message);
            } catch (Exception e) {
                Mcqq.LOGGER.error("error while handling message", e);
            }
        }
    }

    public void sendQQMessage(SendMessageRequest request) {
        WebSocketRequest<SendMessageRequest> webSocketRequest = new WebSocketRequest<>(
            SendMessageRequest.ACTION, 
            request, 
            "echo");

        send(gson.toJson(webSocketRequest));
    }
}
