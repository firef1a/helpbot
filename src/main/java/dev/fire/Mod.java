package dev.fire;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.fire.chat.ResponseManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
	public static final String MOD_NAME = "HelpBot";
	public static final String MOD_ID = "helpbot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static ResponseManager responseManager;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		responseManager = new ResponseManager();

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			Mod.consoleLog("saving data");
			Mod.responseManager.save();
		});

	}

	public static void consoleLog(String txt) {
		Mod.LOGGER.info(txt);
	}


}