package dicemc.money;

import com.mojang.logging.LogUtils;
import dicemc.money.api.MoneyManager;
import dicemc.money.commands.AccountCommandRoot;
import dicemc.money.commands.AccountCommandTop;
import dicemc.money.commands.ShopCommandBuilder;
import dicemc.money.compat.SectionProtectionCompat;
import dicemc.money.compat.ftbquests.FTBQHandler;
import dicemc.money.setup.Config;
import dicemc.money.storage.DatabaseManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MoneyMod.MOD_ID)
public class MoneyMod {
	public static final String MOD_ID = "dicemcmm";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static DatabaseManager dbm;
	
	public MoneyMod() {		
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);		
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		
		if (ModList.get().isLoaded( "ftbquests" ))
        	FTBQHandler.init();
		if (ModList.get().isLoaded("sectionprotection"))
			SectionProtectionCompat.init();
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void commonSetup(final FMLCommonSetupEvent event) {

	}
	
	@SubscribeEvent 
	public void onServerStart(ServerStartingEvent event ) {
		MoneyManager.get().setWorld(event.getServer().overworld());
		if (Config.ENABLE_HISTORY.get()) {
			String worldname = getWorldName(event.getServer().getWorldData().getLevelName());
			String urlIn = event.getServer().getServerDirectory().getAbsolutePath() + "\\saves\\" + worldname +"\\";
			dbm = new DatabaseManager(worldname, urlIn);
		}
	}
	
	@SubscribeEvent
	public void onCommandRegister(RegisterCommandsEvent event) {
		AccountCommandRoot.register(event.getDispatcher());
		ShopCommandBuilder.register(event.getDispatcher());
		AccountCommandTop.register(event.getDispatcher());
	}
	
	//This enum is just for the establishment of later types.
	//Just forward thinking for expansion.
	public static enum AcctTypes{
		PLAYER(new ResourceLocation(MOD_ID, "player")),
		SERVER(new ResourceLocation(MOD_ID, "server"));
		
		public ResourceLocation key;
		
		AcctTypes(ResourceLocation res) {key = res;}
	}
	
	private static String getWorldName(String raw) {
		int start = raw.indexOf("[")+1;
		int end = raw.contains("]") ? raw.length()-1 : raw.length();
		start = (start >= 0 && start < end) ? start : 0;
		end = (end >= 0) ? end : 0;
		return raw.substring(start, end);
	}
}
