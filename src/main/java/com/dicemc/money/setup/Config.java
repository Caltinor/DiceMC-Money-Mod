package com.dicemc.money.setup;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	public static final String CATEGORY_SERVER = "server";
	public static final String CATEGORY_CLIENT = "client";
	
	public static final String SUB_CATEGORY_CLIENT = "Client";
	public static final String SUB_CATEGORY_SERVER = "Server";
	
	//Data Storage Scheme Variables
	public static ForgeConfigSpec.ConfigValue<Boolean> SETTING;
	//Primary Database config values
	public static ForgeConfigSpec.ConfigValue<String> DB_PORT;
	public static ForgeConfigSpec.ConfigValue<String> DB_NAME;
	public static ForgeConfigSpec.ConfigValue<String> DB_SERVICE;
	public static ForgeConfigSpec.ConfigValue<String> DB_URL;
	public static ForgeConfigSpec.ConfigValue<String> DB_USER;
	public static ForgeConfigSpec.ConfigValue<String> DB_PASS;
	//Misc variables
	public static ForgeConfigSpec.ConfigValue<Double> STARTING_FUNDS;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		
		CLIENT_BUILDER.comment("Client Settings").push(CATEGORY_CLIENT);
		setupClient(CLIENT_BUILDER);
		CLIENT_BUILDER.pop();
		
		SERVER_BUILDER.comment("Server Settings").push(CATEGORY_SERVER);
		setupServer(SERVER_BUILDER);
		SERVER_BUILDER.pop();
		
		CLIENT_CONFIG = CLIENT_BUILDER.build();
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	private static void setupClient(ForgeConfigSpec.Builder builder) {
		builder.comment("Client Settings").push(SUB_CATEGORY_CLIENT);
		
		SETTING = builder.comment("does nothing")
				.define("setting", false);
		
		builder.pop();		
	}
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("Server Settings").push(SUB_CATEGORY_SERVER);
		
		//Database location
		DB_PORT = builder.comment("Database port")
				.define("port", "");
		DB_NAME = builder.comment("Database name")
				.define("name", "accounts");
		DB_SERVICE = builder.comment("Database Service syntax")
				.define("service", "h2");
		DB_URL = builder.comment("Database URL if not using remote, use your mod's file path")
				.define("url", "");
		DB_USER = builder.comment("Database Username")
				.define("user", "sa");
		DB_PASS = builder.comment("Database Password")
				.define("pass", "");
		//Misc Variables
		STARTING_FUNDS = builder.comment("The amount of money a new player starts with when first joining the world.")
				.defineInRange("starting_funds", 1000D, 0, Double.MAX_VALUE);
		
		builder.pop();		
	}
}
