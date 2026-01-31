package com.nexora;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexora.config.McqqConfig;
import com.nexora.daemon.McqqDaemon;
import com.nexora.onebot.events.message.array.MessageSegment;
import com.nexora.onebot.events.message.array.MessageSegmentType;
import com.nexora.onebot.events.message.array.ReplySegmentData;
import com.nexora.onebot.events.message.array.TextSegmentData;
import com.nexora.screen.ViewImageScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.List;
import java.util.OptionalInt;

public class McqqClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerDaemonLifecycle();
		registerCommands();
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				literal("mcqq")
					.then(literal("config")
						.executes(context -> {
							showConfig();
							return 1;
						}))
					.then(literal("reload")
						.executes(context -> {
							restartDaemon();
							return 1;
						}))
					.then(literal("img")
						.then(argument("file", StringArgumentType.string())
							.executes(context -> {
								String imageUrl = context.getArgument("file", String.class);
								viewImage(imageUrl);
								return 1;
							})))
			);

			dispatcher.register(
				literal("qq")
					.then(argument("content", StringArgumentType.greedyString())
						.executes(context -> {
							String message = context.getArgument("content", String.class);
							// TODO: parse to a message array
							sendMessage(message);
							return 1;
						}))
			);

			dispatcher.register(
				literal("qqr")
					.then(argument("id", IntegerArgumentType.integer())
						.then(argument("content", StringArgumentType.greedyString())
							.executes(context -> {
								Integer replyMessageId = context.getArgument("id", Integer.class);
								String message = context.getArgument("content", String.class);
								sendReplyMessage(replyMessageId, message);
								return 1;
							})
						)
					)
			);
		});
	}

	private void sendReplyMessage(Integer replyMessageId, String message) {
		McqqDaemon.getInstance().sendMessage(List.of(
			new MessageSegment<ReplySegmentData>(MessageSegmentType.REPLY, new ReplySegmentData(replyMessageId)),
			new MessageSegment<TextSegmentData>(MessageSegmentType.TEXT, new TextSegmentData(message))
		));

		// TODO: Show info of the message replied got from napcat api
		Minecraft.getInstance().gui.getChat().addMessage(
			Component.literal("[REPLY] You").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(": " + message).withStyle(ChatFormatting.WHITE))
		);
	}

	private void viewImage(String file) {
		Mcqq.LOGGER.info("Viewing image: {}", file);
		
		Screen screen = new ViewImageScreen(file);
		
		Minecraft.getInstance().tell(() -> {
			Minecraft.getInstance().setScreen(screen);
		});
	}

	private void sendMessage(String message) {
		Minecraft minecraft = Minecraft.getInstance();
		String formattedMessage = String.format("[Minecraft %s] %s: %s", 
			minecraft.getLaunchedVersion(),
			minecraft.player.getName().getString(),
			message
		);

		McqqDaemon.getInstance().sendMessage(formattedMessage);
		minecraft.gui.getChat().addMessage(
			Component.literal("You").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(": " + message).withStyle(ChatFormatting.WHITE))
		);
	}

	private void showConfig() {
		McqqConfig config = McqqConfig.getConfig();

		ChatComponent chat = Minecraft.getInstance().gui.getChat();
	
		chat.addMessage(Component.literal("Mcqq config")
			.withStyle(ChatFormatting.BOLD)
			.withStyle(ChatFormatting.YELLOW)
			.withStyle(ChatFormatting.UNDERLINE));

		OptionalInt port = config.getNapcatPort();

		String portDisplay = port.isPresent() ? Integer.toString(port.getAsInt()) : "<Unset>";

		chat.addMessage(Component.literal("URL: " + config.getNapcatUrl().orElse("<Unset>")));
		chat.addMessage(Component.literal("Port: " + portDisplay));
		chat.addMessage(Component.literal("Token: " + config.getNapcatToken().orElse("<Unset>")));
	}

	private void restartDaemon() {
		McqqDaemon daemon = McqqDaemon.getInstance();
		if (daemon.isRunning()) {
			daemon.stop();
		}
		daemon.start();
	}

	private void registerDaemonLifecycle() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			McqqDaemon.getInstance().start();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
		    McqqDaemon.getInstance().stop();
		});
	}
}