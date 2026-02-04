package com.nexora;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nexora.config.McqqConfig;
import com.nexora.daemon.McqqDaemon;
import com.nexora.onebot.events.message.array.ImageSegmentData;
import com.nexora.onebot.events.message.array.MessageSegment;
import com.nexora.onebot.events.message.array.MessageSegmentType;
import com.nexora.onebot.events.message.array.ReplySegmentData;
import com.nexora.onebot.events.message.array.TextSegmentData;
import com.nexora.onebot.events.message.array.ImageSegmentData.ImageType;
import com.nexora.screen.ViewImageScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.io.File;
import java.io.IOException;
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

			dispatcher.register(
				literal("qqsc").executes(context -> {
					shareScreenShot();
					return 1;
				})
					.then(literal("sharesystem")
						.then(argument("path", StringArgumentType.greedyString())
							.executes(context -> {
								String path = context.getArgument("path", String.class);
								shareScreenShotInPath(path);
								return 1;
							})
						)
					)
			);
		});
	}

	private void shareScreenShotInPath(String path) {
		Minecraft minecraft = Minecraft.getInstance();
		ChatComponent chat = minecraft.gui.getChat();
		File file = new File(path);

		if (!file.exists()) {
			chat.addMessage(Component.literal(String.format(
				"Given file at path %s doesn't exist!", path))
					.withStyle(ChatFormatting.RED)
			);
			return;
		}

		if (!file.isFile()) {
			chat.addMessage(Component.literal(String.format(
				"Given path %s is not a file!", path))
					.withStyle(ChatFormatting.RED)
			);
			return;
		}

		ImageSegmentData imageSegmentData = new ImageSegmentData();
		imageSegmentData.setFile(path);
		imageSegmentData.setType(ImageType.IMAGE);
		imageSegmentData.setProxy(false);

		McqqDaemon.getInstance().sendMessage(List.of(
			new MessageSegment<TextSegmentData>(MessageSegmentType.TEXT, new TextSegmentData(String.format(
				"screenshot from [%s] %s:", 
				FabricLoader.getInstance().getRawGameVersion(),
				minecraft.player.getName().getString()
			))),
			new MessageSegment<ImageSegmentData>(MessageSegmentType.IMAGE, imageSegmentData)
		));
		
		Minecraft.getInstance().gui.getChat().addMessage(
			Component.literal("You").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" shared a screenshot").withStyle(ChatFormatting.WHITE))
		);
    }

    private void shareScreenShot() {
		File file = Minecraft.getInstance().gameDirectory.toPath()
			.resolve("mcqq_image")
			.resolve(System.currentTimeMillis() + ".png")
			.toFile();

		File directory = file.getParentFile();
		if (!directory.exists()) {
			directory.mkdirs();
		}

		try (NativeImage image = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
			image.writeToFile(file);
			
			ImageSegmentData imageSegmentData = new ImageSegmentData();
			
			imageSegmentData.setType(ImageType.IMAGE);
			imageSegmentData.setFile("file:///" + file.getAbsolutePath());
			
			Minecraft minecraft = Minecraft.getInstance();

			McqqDaemon.getInstance().sendMessage(List.of(
				new MessageSegment<TextSegmentData>(MessageSegmentType.TEXT, new TextSegmentData(String.format(
					"screenshot from [%s] %s:", 
					FabricLoader.getInstance().getRawGameVersion(),
					minecraft.player.getName().getString()
				))),
				new MessageSegment<ImageSegmentData>(MessageSegmentType.IMAGE, imageSegmentData)
			));
			
			Minecraft.getInstance().gui.getChat().addMessage(
				Component.literal("You").withStyle(ChatFormatting.GREEN)
				.append(Component.literal(" shared a screenshot").withStyle(ChatFormatting.WHITE))
			);
		} catch (IOException e) {
			Mcqq.LOGGER.error("Failed to take screenshot", e);
			Minecraft.getInstance().gui.getChat().addMessage(
				Component.literal("Failed to take screenshot, this might be an error")
				.withStyle(ChatFormatting.RED));
		} finally {
			file.deleteOnExit();
		}
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
		String formattedMessage = String.format("[%s] %s: %s", 
			FabricLoader.getInstance().getRawGameVersion(),
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