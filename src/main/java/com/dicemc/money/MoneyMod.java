package com.dicemc.money;

import java.util.UUID;
import com.dicemc.money.api.IAccountProvider;
import com.dicemc.money.client.ContainerAccountManager;
import com.dicemc.money.item.MoneybagCombineRecipe;
import com.dicemc.money.setup.ClientSetup;
import com.dicemc.money.setup.CommonSetup;
import com.dicemc.money.setup.Config;
import com.dicemc.money.setup.Registration;
import com.dicemc.money.storage.DatabaseManager;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoneyMod.MOD_ID)
public class MoneyMod {
	public static final String MOD_ID = "dicemcmm";
	public static final UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	public static final ResourceLocation playerAccounts = new ResourceLocation(MOD_ID, "players");
	public static DatabaseManager dbm;
	public static IAccountProvider AcctProvider;
	
	public MoneyMod() {		
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
		
		Registration.init();
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::init);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ContainerType.class, this::registerContainers);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SpecialRecipeSerializer<>(MoneybagCombineRecipe::new).setRegistryName("dicemcmm:moneybag_combine")
        );
    }
	
	public void registerContainers(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                IForgeContainerType.create(ContainerAccountManager::new).setRegistryName("gui_container")
        );
    }
}
