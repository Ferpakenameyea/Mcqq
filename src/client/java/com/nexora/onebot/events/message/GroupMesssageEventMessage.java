package com.nexora.onebot.events.message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.nexora.onebot.events.message.array.MessageSegment;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import static net.minecraft.network.chat.Component.literal;


public final class GroupMesssageEventMessage extends MessageEventMessage {
    @SerializedName("group_id")
    private long groupId;

    @SerializedName("sub_type")
    private SubType subType;

    @SerializedName("sender")
    private GroupMessageSenderInfo sender;

    public long getGroupId() {
        return groupId;
    }

    public SubType getSubType() {
        return subType;
    }

    public enum SubType {
        @SerializedName("normal")
        NORMAL,
        @SerializedName("anonymous")
        ANONYMOUS,
        @SerializedName("notice")
        NOTICE
    }

    public List<Component> buildMinecraftMessage() {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());

        return List.of(
            literal("[group]")
                .append(
                    literal(String.format("%s (%s) %d ",
                        sender.getCard().orElse("unknown"), 
                        sender.getNickname().orElse("unknown"),
                        userId
                    )).withStyle(ChatFormatting.GOLD)
                )
                .append(literal(String.format("%d:%d:%d", 
                    localDateTime.getHour(),
                    localDateTime.getMinute(),
                    localDateTime.getSecond()
                ))).withStyle(ChatFormatting.GRAY),
            buildMinecraftLiteral());
    }

    private Component buildMinecraftLiteral() {
        return getDeserializedMessages().stream()
            .map(MessageSegment::toComponent)
            .reduce((c1, c2) -> c1.append(c2))
            .orElse(literal("<No message>"));
    }
}
