package dicemc.money;

import com.mojang.logging.LogUtils;
import dicemc.money.api.MoneyManager;
import dicemc.money.commands.AccountCommandRoot;
import dicemc.money.commands.AccountCommandTop;
import dicemc.money.commands.ShopCommandBuilder;
import dicemc.money.compat.ftbquests.FTBQHandler;
import dicemc.money.setup.Config;
import dicemc.money.storage.DatabaseManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(MoneyMod.MOD_ID)
public class MoneyMod {
	public static final String MOD_ID = "dicemcmm";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static DatabaseManager dbm;
	
	public MoneyMod(IEventBus bus, ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
		
		if (ModList.get().isLoaded( "ftbquests" ))
        	FTBQHandler.init();

		NeoForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent 
	public void onServerStart(ServerStartingEvent event ) {
		MoneyManager.get().setWorld(event.getServer().overworld());
		if (Config.ENABLE_HISTORY.get()) {
			String worldname = getWorldName(event.getServer().getWorldData().getLevelName());
			String urlIn = event.getServer().getServerDirectory().toAbsolutePath() + "\\saves\\" + worldname +"\\";
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
	public enum AcctTypes{
		PLAYER(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player")),
		SERVER(ResourceLocation.fromNamespaceAndPath(MOD_ID, "server"));
		
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
