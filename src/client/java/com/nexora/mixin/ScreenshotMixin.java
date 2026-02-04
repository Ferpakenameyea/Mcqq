package com.nexora.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    
    @ModifyArg(
        method = "method_1661",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"
        ),
        index = 0
    )
    private static Object modifyScreenshotMessage(Object original) {
        if (original instanceof MutableComponent component) {
            if (component.getContents() instanceof TranslatableContents contents) {
                MutableComponent mutableComponent = (MutableComponent) contents.getArgs()[0];
                ClickEvent clickEvent = mutableComponent.getStyle().getClickEvent();
                String absolutePath = clickEvent.getValue();
                
                return component.append(
                    Component.literal(" ")
                        .append(Component.literal("[@]").withStyle(style -> 
                            style.withColor(ChatFormatting.DARK_GRAY)
                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Share this image?")))
                                 .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qqsc sharesystem " + absolutePath))))
                );
            }
        }
        return original;
    }
}
