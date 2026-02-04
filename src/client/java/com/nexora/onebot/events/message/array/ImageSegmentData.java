package com.nexora.onebot.events.message.array;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

// all fields used in serialization through gson
@SuppressWarnings("unused")
public final class ImageSegmentData extends MessageSegmentData {
    private String file;

    private String type;

    private String url;

    private int cache;

    private int proxy;

    private int timeout;

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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setCache(boolean cache) {
        this.cache = cache ? 1 : 0;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy ? 1 : 0;
    }

    public String getUrl() {
        return url;
    }

    public ImageType getType() {
        return type == null ? ImageType.IMAGE : ImageType.FLASH;
    }

    public void setType(ImageType imageType) {
        this.type = imageType == ImageType.FLASH ? "flash" : null;
    }

    public enum ImageType {
        IMAGE, FLASH
    }

    public void setFile(String file) {
        this.file = file;
    }
}
