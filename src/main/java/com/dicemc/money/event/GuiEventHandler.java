package com.dicemc.money.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber
public class GuiEventHandler {
	@SuppressWarnings({ "static-access", "resource" })
	@SubscribeEvent
	public static void onInventoryLoad(GuiScreenEvent.DrawScreenEvent event) {
		if (event.getGui() instanceof InventoryScreen || event.getGui() instanceof CreativeScreen) {
			event.getGui().drawString(event.getMatrixStack(), Minecraft.getInstance().fontRenderer, new TranslationTextComponent("guiinventory.tip").mergeStyle(TextFormatting.YELLOW), 10, 5, 5);
		}
	}
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onKeyPress(GuiScreenEvent.KeyboardKeyPressedEvent event) {
		if ((event.getGui() instanceof InventoryScreen || event.getGui() instanceof CreativeScreen) && event.getKeyCode() == 65) {
			Minecraft.getInstance().player.sendChatMessage("/account gui");
		}
	}
}
