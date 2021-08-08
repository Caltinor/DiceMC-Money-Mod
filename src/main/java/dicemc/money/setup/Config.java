package dicemc.money.setup;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;

	public static final String CATEGORY_SERVER = "server";
	
	public static final String SUB_CATEGORY_SERVER = "Server";
	
	//Data Storage Scheme Variables
	public static ForgeConfigSpec.ConfigValue<Boolean> SETTING;
	//Misc variables
	public static ForgeConfigSpec.ConfigValue<Double> STARTING_FUNDS;
	public static ForgeConfigSpec.ConfigValue<String> CURRENCY_SYMBOL;
	public static ForgeConfigSpec.ConfigValue<Integer> ADMIN_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Integer> SHOP_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> LOSS_ON_DEATH;
	public static ForgeConfigSpec.ConfigValue<Integer>	TOP_SIZE;
	public static ForgeConfigSpec.ConfigValue<Boolean> ENABLE_HISTORY;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		
		SERVER_BUILDER.comment("Server Settings").push(CATEGORY_SERVER);
		setupServer(SERVER_BUILDER);
		SERVER_BUILDER.pop();
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("Server Settings").push(SUB_CATEGORY_SERVER);
		//Misc Variables
		STARTING_FUNDS = builder.comment("The amount of money a new player starts with when first joining the world.")
				.defineInRange("starting_funds", 1000D, 0, Double.MAX_VALUE);
		CURRENCY_SYMBOL = builder.comment("the character(s) to precede money values when displayed")
				.define("currency symbol", "$");
		ADMIN_LEVEL = builder.comment("the op level permitted to use admin commands")
				.defineInRange("admin level", 2, 0, 4, Integer.class);
		SHOP_LEVEL = builder.comment("The minimum permission level to create basic shops.  default= 0 = all players")
				.defineInRange("shop level", 0, 0, 4, Integer.class);
		LOSS_ON_DEATH = builder.comment("a percentage of the player's account that is lost on death, in decimal form. default = 0%. 0.5 = 50%")
				.defineInRange("loss on death", 0d, 0d, 1d);
		TOP_SIZE = builder.comment("The number of players to be displayed when the top command is used.  set to zero to disable")
				.define("top size", 3);
		ENABLE_HISTORY = builder.comment("This setting turns enables transactions to be recorded using the H2 embedded database system."
				, "leaving this disabled will prevent the creation of the database.  It is recommended that"
				, "you use an application like DBeaver to view and query your data.")
				.define("enable_history", false);
		
		builder.pop();		
	}
}
