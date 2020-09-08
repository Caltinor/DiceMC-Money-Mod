package com.dicemc.money.setup;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.api.MoneyApi;
import com.dicemc.money.commands.AccountCommandRoot;
import com.dicemc.money.core.AccountManager;
import com.dicemc.money.item.MoneybagCombineRecipe;
import com.dicemc.money.storage.DatabaseManager;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

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
	public static void onServerStart(FMLServerAboutToStartEvent event) {
		MoneyMod.dbm = new DatabaseManager();
	}
}
