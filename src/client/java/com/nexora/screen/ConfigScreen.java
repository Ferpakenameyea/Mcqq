package com.nexora.screen;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nexora.Mcqq;
import com.nexora.config.McqqConfig;

public class ConfigScreen extends Screen {
    private EditBox urlEditBox;
    private EditBox portEditBox;
    private EditBox tokenEditBox;

    private Button testConnectionButton;
    private Button saveButton;
    private Button cancelButton;

    private volatile TestStatus testStatus = TestStatus.NOT_TESTED;
    private final AtomicBoolean isTesting = new AtomicBoolean(false);

    public ConfigScreen(Screen parent) {
        super(Component.literal("Mcqq Config"));
    }
    
    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 2 - 65;
    
        urlEditBox = new EditBox(this.font, x, y, 140, 20, Component.literal("napcat url"));
        portEditBox = new EditBox(this.font, x + urlEditBox.getWidth() + 10, urlEditBox.getY(), 50, 20, Component.literal("napcat Port"));
        portEditBox.setFilter(text -> {
            if (text.isEmpty()) {
                return true;
            }
            if (!text.chars().allMatch(Character::isDigit)) {
                return false;
            };

            try {
                int v = Integer.parseInt(text);
                return v >= 0 && v <= 65535;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        
        tokenEditBox = new EditBox(this.font, x, y + 40, 200, 20, Component.literal("napcat Token"));

        testConnectionButton = Button.builder(Component.literal("test connection"), button -> {
                runTestConnection();
                testConnectionButton.setFocused(false);
            })
            .bounds(tokenEditBox.getX(), tokenEditBox.getY() + 40, 120, 20)
            .build();

        saveButton = Button.builder(Component.literal("save"), button -> {
                McqqConfig config = McqqConfig.getConfig();
                config.setNapcatUrl(urlEditBox.getValue());
                config.setNapcatPort(Integer.parseInt(portEditBox.getValue()));
                config.setNapcatToken(tokenEditBox.getValue());
                config.persist();
                this.onClose();
            }).bounds(width / 2 - 100, tokenEditBox.getY() + 80, 90, 20).build();

        cancelButton = Button.builder(Component.literal("cancel"), button -> {
                this.onClose();
            }).bounds(width / 2 + 10, tokenEditBox.getY() + 80, 90, 20).build();

        addRenderableWidget(urlEditBox);
        addRenderableWidget(portEditBox);
        addRenderableWidget(tokenEditBox);

        addRenderableWidget(testConnectionButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);

        McqqConfig config = McqqConfig.getConfig();
        urlEditBox.setValue(config.getNapcatUrl().orElse("localhost"));
        portEditBox.setValue(String.valueOf(config.getNapcatPort().orElse(3001)));
        tokenEditBox.setValue(config.getNapcatToken().orElse(""));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font, "Mcqq Config", width / 2, 20, 0xffffff);

        guiGraphics.drawString(font, "NatcatIP", urlEditBox.getX(), urlEditBox.getY() - 10, 0xffffff);
        guiGraphics.drawString(font, "Port", portEditBox.getX(), portEditBox.getY() - 10, 0xffffff);
        guiGraphics.drawString(font, "Token", tokenEditBox.getX(), tokenEditBox.getY() - 10, 0xffffff);
        guiGraphics.drawString(font, 
            testStatus.message(), 
            testConnectionButton.getX() + testConnectionButton.getWidth() + 10, 
            testConnectionButton.getY() + (testConnectionButton.getHeight() - font.lineHeight) / 2, 
            testStatus.getDisplayColor());
    }

    private void runTestConnection() {
        if (!isTesting.compareAndSet(false, true)) {
            return;
        }
        testStatus = TestStatus.TESTING;
        String uriRaw = String.format("ws://%s:%d", 
            urlEditBox.getValue(), 
            Integer.parseInt(portEditBox.getValue()), 
            tokenEditBox.getValue());

        Thread testingThread = new Thread(() -> {
            try {
                URI uri = new URI(uriRaw);
                WebSocketClient client = buildTestClient(uri, tokenEditBox.getValue());
                if (!client.connectBlocking(5, TimeUnit.SECONDS)) {
                    testStatus = new TestStatus("Connection failed", false);
                }
            } catch (Exception e) {
                Mcqq.LOGGER.error("failed to connect to napcat for uri: %s", uriRaw);
                Mcqq.LOGGER.error("exception:", e);
                testStatus = new TestStatus(e.getMessage(), false);
            } finally {
                isTesting.set(false);
            }
        });
        
        testingThread.start();
    }

    private WebSocketClient buildTestClient(URI uri, String token) {
        return new WebSocketClient(uri, Map.<String, String>of("Authorization", "Bearer " + token)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
            }

            @Override
            public void onMessage(String message) {
                try {
                    JsonElement element = JsonParser.parseString(message);
                    JsonObject jsonObject = element.getAsJsonObject();
                    if (jsonObject.has("status") && jsonObject.get("status").getAsString().equals("failed")) {
                        testStatus = new TestStatus("Auth failed", false);
                    } else if (jsonObject.has("sub_type")
                            && jsonObject.get("sub_type").getAsString().equals("connect")) {
                        testStatus = new TestStatus("Connected", true);
                    }
                } finally {
                    close();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
            }

            @Override
            public void onError(Exception ex) {
            }
        };
    }

    private static record TestStatus(String message, boolean isSuccess) {
        public static final TestStatus NOT_TESTED = new TestStatus("not tested", false);
        public static final TestStatus TESTING = new TestStatus("connecting...", false);

        public int getDisplayColor() {
            if (this == NOT_TESTED || this == TESTING) {
                return 0xffffff;
            }

            return isSuccess ? 0x00ff00 : 0xff0000;
        }
    }
}
