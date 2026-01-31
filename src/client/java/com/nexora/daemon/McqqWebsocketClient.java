package com.nexora.daemon;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.nexora.Mcqq;
import com.nexora.common.Pot;
import com.nexora.config.McqqConfig;
import com.nexora.daemon.handler.MessageEventHandlers;
import com.nexora.daemon.handler.MetaEventHandlers;
import com.nexora.onebot.OnebotMessage;
import com.nexora.onebot.api.GetImageRequest;
import com.nexora.onebot.api.ImageResponse;
import com.nexora.onebot.api.Response;
import com.nexora.onebot.api.SendMessageRequest;
import com.nexora.onebot.api.WebSocketRequest;
import com.nexora.onebot.events.message.GroupMesssageEventMessage;
import com.nexora.onebot.events.meta.LifeCycleMessage;

public class McqqWebsocketClient extends WebSocketClient {

    private static final Gson gson = new Gson();

    private final Map<String, RequestInfo> requestMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends OnebotMessage>, Consumer<OnebotMessage>> handlerMap;

    private Thread cleanThread = null;

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
    public void onOpen(ServerHandshake handshakedata) {
        cleanThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    long now = System.currentTimeMillis();
                    requestMap.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                Mcqq.LOGGER.info("clean thread interrupted");
            }
            Mcqq.LOGGER.info("clean thread stopped");
        });

        cleanThread.start();
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            if (isResponse(jsonObject)) {
                putResponse(jsonObject);  
            } else {
                OnebotMessage.deserializeFrom(jsonObject)
                    .ifPresent(this::react);
            }
        } catch (JsonParseException | IllegalStateException e) {
            
        }
    }

    private void putResponse(JsonObject jsonObject) {
        String uuid = jsonObject.get("echo").getAsString();
        RequestInfo requestInfo = requestMap.remove(uuid);

        if (requestInfo != null) {
            Type type = requestInfo.expectReturnType();
            Object value = gson.fromJson(jsonObject, type);
            
            @SuppressWarnings("unchecked")
            Pot<Object> pot = (Pot<Object>)requestInfo.pot();
            
            pot.set(value);
        }
    }

    private static boolean isResponse(JsonObject jsonObject) {
        return jsonObject.has("echo");
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

        if (cleanThread != null) {
            cleanThread.interrupt();
            cleanThread = null;
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

    public Pot<Response<ImageResponse>> getImage(GetImageRequest request) {
        Pot<Response<ImageResponse>> pot = new Pot<>();

        String uuidString = UUID.randomUUID().toString();

        WebSocketRequest<GetImageRequest> webSocketRequest = new WebSocketRequest<>(
            GetImageRequest.ACTION, 
            request, 
            uuidString);

        Type type = new TypeToken<Response<ImageResponse>>() {}.getType();
        
        requestMap.put(uuidString, new RequestInfo(
            uuidString, 
            pot, 
            type,
            /*expire time*/ System.currentTimeMillis() + 5100
        ));

        send(gson.toJson(webSocketRequest));
        
        return pot;
    }
}

record RequestInfo(
    String requestId, 
    Pot<?> pot,
    Type expectReturnType,
    long expireTime) 
{
    public boolean isExpired(long nowMillis) {
        return nowMillis > expireTime;
    }
}