package com.nexora;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mcqq implements ModInitializer {

	public static final String MOD_ID = "mcqq";
	
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("mod {} loaded.", MOD_ID);
	}
}