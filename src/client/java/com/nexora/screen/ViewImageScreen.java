package com.nexora.screen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mojang.blaze3d.platform.NativeImage;
import com.nexora.Mcqq;
import com.nexora.daemon.McqqDaemon;
import com.nexora.onebot.api.ImageResponse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ViewImageScreen extends Screen {
    private final String file;

    private volatile ResourceLocation resourceLocation;
    private volatile NativeImage nativeImage;

    private volatile ImageGetStatus status = ImageGetStatus.LOADING;
    private final Thread downloadThread = new Thread(this::downloadImage);

    private Lock lock = new ReentrantLock();

    private Button exitButton;

    public ViewImageScreen(String file) {
        super(Component.literal("mcqq image view"));
        this.file = file;
        downloadThread.start();
    }

    @Override
    protected void init() {
        exitButton = Button.builder(Component.literal("exit"), button -> {
            Minecraft.getInstance().setScreen(null);
        })
            .bounds(width / 2 - 40, height - 30, 80, 20)
            .build();
        addRenderableWidget(exitButton);
    }
    
    private void downloadImage() {
        if (resourceLocation != null) {
            return;
        }
        McqqDaemon.getInstance().getImage(file).ifPresentOrElse(result -> {
            getImageBinary(result).ifPresentOrElse(
                this::tryRenderImage, 
                () -> status = ImageGetStatus.ERROR);
        }, () -> status = ImageGetStatus.ERROR);
    }

    private void tryRenderImage(byte[] image) {
        try {
            createTexture(image);
            if (status == ImageGetStatus.DESTROYED) {
                return;
            }
            status = ImageGetStatus.READY;
            Mcqq.LOGGER.info("Image loaded successfully");
        } catch (IOException e) {
            Mcqq.LOGGER.error("Failed to create texture", e);
            status = ImageGetStatus.ERROR;
        }
    }

    private void createTexture(byte[] image) throws IOException {
        nativeImage = NativeImage.read(new ByteArrayInputStream(image));
        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        lock.lock();
        try {
            if (status == ImageGetStatus.DESTROYED) {
                return;
            }
            resourceLocation = textureManager.register("mcqq_image" + UUID.randomUUID().toString(), dynamicTexture);
        } finally {
            lock.unlock();
        }
    }

    private Optional<byte[]> getImageBinary(ImageResponse response) {
        try {
            Optional<byte[]> image = getImageFromLocal(response);
            if (!image.isPresent()) {
                image = getImageFromUrl(response);
            }

            return image;
        } catch (RuntimeException e) {
            Mcqq.LOGGER.error("Failed to read image file from local", e);
            return Optional.empty();
        }
    }

    private Optional<byte[]> getImageFromUrl(ImageResponse response) {
        return response.getUrl()
            .map(url -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .build();
                try {
                    HttpResponse<byte[]> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    if (httpResponse.statusCode() != 200) {
                        return null;
                    }
                    return httpResponse.body();
                } catch (Exception e) {
                    Mcqq.LOGGER.error("failed to get image from url {}", url);
                }
                return null;
            });
    }

    private Optional<byte[]> getImageFromLocal(ImageResponse response) {
        return response.getFile()
            .map(Paths::get)
            .map(path -> {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        
        if (status == ImageGetStatus.READY) {
            double scaleX = (double) width / nativeImage.getWidth();
            double scaleY = (double) height / nativeImage.getHeight();
            double scale = Math.min(scaleX, scaleY);

            scale *= 0.75;

            int displayWidth = (int) (nativeImage.getWidth() * scale);
            int displayHeight = (int) (nativeImage.getHeight() * scale);

            int x = (width - displayWidth) / 2;
            int y = (height - displayHeight) / 2;

            guiGraphics.blit(
                resourceLocation,
                x, y,
                0, 0,
                displayWidth, 
                displayHeight,
                displayWidth,
                displayHeight
            );

            guiGraphics.drawCenteredString(
                font, 
                "QQ Image View", width / 2 , 10, 0xFFFFFF);
        } else if (status == ImageGetStatus.ERROR) {
            guiGraphics.drawCenteredString(
                font,
                "Failed to load image", width / 2, height / 2, 0xFF0000);
        } else if (status == ImageGetStatus.LOADING) {
            guiGraphics.drawCenteredString(
                font,
                "Loading...", width / 2, height / 2, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        lock.lock();
        try {
            status = ImageGetStatus.DESTROYED;
            downloadThread.interrupt();
        } finally {
            lock.unlock();
        }

        if (resourceLocation != null) {
            Minecraft.getInstance()
                .getTextureManager()
                .release(resourceLocation);
            Mcqq.LOGGER.info("Image texture {} released", resourceLocation.getPath());
        }
    }

    private enum ImageGetStatus {
        LOADING,
        READY,
        ERROR,
        DESTROYED
    }
}
