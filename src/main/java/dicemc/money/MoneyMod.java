package dicemc.money;

import dicemc.money.FTBQuests.FTBQHandler;
import dicemc.money.api.MoneyManager;
import dicemc.money.commands.AccountCommandRoot;
import dicemc.money.commands.AccountCommandTop;
import dicemc.money.commands.ShopCommandBuilder;
import dicemc.money.setup.Config;
import dicemc.money.storage.DatabaseManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoneyMod.MOD_ID)
public class MoneyMod {
	public static final String MOD_ID = "dicemcmm";
	public static DatabaseManager dbm;
	
	public MoneyMod() {		
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);		
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
		
		if( ModList.get().isLoaded( "ftbquests" ) )
        {
            FTBQHandler.init();
        }
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void commonSetup(final FMLCommonSetupEvent event) {

	}
	
	@SubscribeEvent 
	public void onServerStart(FMLServerStartingEvent event ) {
		MoneyManager.get().setWorld(event.getServer().overworld());
		System.out.println(event.getServer().getWorldData().toString());
		if (Config.ENABLE_HISTORY.get()) {
			String worldname = getWorldName(event.getServer().getWorldData().toString());
			System.out.println(worldname);
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
		int end = raw.length()-1;
		start = (start >= 0 && start < end) ? start : 0;
		end = (end >= 0) ? end : 0;
		return raw.substring(start, end);
	}
}
