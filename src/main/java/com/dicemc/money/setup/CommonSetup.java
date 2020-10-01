package com.dicemc.money.setup;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.api.MoneyApi;
import com.dicemc.money.commands.AccountCommandRoot;
import com.dicemc.money.core.AccountManager;
import com.dicemc.money.storage.DatabaseManager;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = MoneyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonSetup {
	
	public static final ItemGroup ITEM_GROUP = new ItemGroup(MoneyMod.MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(Registration.MONEYBAG.get());
		}
	};
	
	public static void init(final FMLCommonSetupEvent event) {
		Networking.registerMessages();
		MoneyMod.AcctProvider = new MoneyApi();
		MoneyMod.AcctProvider.setAPI(new AccountManager());
	}
	
	@SubscribeEvent
	public static void onCommandRegister(RegisterCommandsEvent event) {
		AccountCommandRoot.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onServerStart(FMLServerStartingEvent event) {
		String worldname = getWorldName(event.getServer().func_241755_D_().toString());
		System.out.println(worldname);
		String urlIn = Config.DB_LOCAL.get() 
				? event.getServer().getDataDirectory().getAbsolutePath() + "\\saves\\" + worldname +"\\"
				: Config.DB_URL.get();
		MoneyMod.dbm = new DatabaseManager(worldname, urlIn);
	}
	
	private static String getWorldName(String raw) {
		int start = raw.indexOf("[")+1;
		int end = raw.length()-1;
		start = (start >= 0 && start < end) ? start : 0;
		end = (end >= 0) ? end : 0;
		return raw.substring(start, end);
	}
}
