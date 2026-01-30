package com.nexora.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nexora.Mcqq;

import net.fabricmc.loader.api.FabricLoader;

public final class McqqConfig {
    private static McqqConfig instance;
    private static final Gson gsonInstance = new Gson();
    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mcqq.json");

    private String napcatUrl = "localhost";
    private Integer napcatPort = 3001;
    private String napcatToken = "";

    public Optional<String> getWebsocketUrl() {
        if (!connectable()) {
            return Optional.empty();
        }

        String wsUrl = String.format("ws://%s:%d", napcatUrl, napcatPort);
        return Optional.of(wsUrl);
    }

    public boolean connectable() {
        return napcatUrl != null && napcatPort != null && napcatToken != null;
    }

    private static Optional<McqqConfig> tryLoadConfig() {
        if (!Files.exists(configPath)) {
            return Optional.empty();
        }

        try {
            String configString = Files.readString(configPath);
            McqqConfig config = gsonInstance.fromJson(configString, McqqConfig.class);
        
            return Optional.of(config);
        } catch (IOException e) {
            Mcqq.LOGGER.error("IOException occurred when loading config.", e);
            return Optional.empty();
        } catch (SecurityException e) {
            Mcqq.LOGGER.error("SecurityException occurred when loading config, ensure that the config file is readable.", e);
            return Optional.empty();
        } catch (JsonSyntaxException e) {
            Mcqq.LOGGER.error("JsonSyntaxException occurred when loading config, there might be a json syntax error.", e);
            return Optional.empty();
        }
    }

    public static McqqConfig getConfig() {
        if (instance == null) {
            instance = tryLoadConfig().orElse(new McqqConfig());
        }

        return instance;
    }

    public void persist() {
        try {
            String configString = gsonInstance.toJson(this);
            Files.writeString(configPath, configString);
        } catch (IOException e) {
            Mcqq.LOGGER.error("IOException occurred when persisting config.", e);
        }
    }

    public Optional<String> getNapcatUrl() {
        return Optional.ofNullable(napcatUrl);
    }

    public void setNapcatUrl(String napcatUrl) {
        this.napcatUrl = napcatUrl;
    }

    public OptionalInt getNapcatPort() {
        return napcatPort == null ? OptionalInt.empty() : OptionalInt.of(napcatPort);
    }

    public void setNapcatPort(int napcatPort) {
        this.napcatPort = napcatPort;
    }

    public Optional<String> getNapcatToken() {
        return Optional.ofNullable(napcatToken);
    }

    public void setNapcatToken(String napcatToken) {
        this.napcatToken = napcatToken;
    }
}
