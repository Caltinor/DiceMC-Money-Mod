package com.dicemc.money.setup;

import com.dicemc.money.client.ContainerAccountManager;
import com.dicemc.money.client.GuiAccountManager;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

	public static void init(final FMLClientSetupEvent event)
	{
	    ScreenManager.registerFactory(ContainerAccountManager.TYPE, GuiAccountManager::new); //(your container type, screen factory)
	}
}
