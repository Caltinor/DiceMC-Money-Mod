package com.dicemc.money.event;

import com.dicemc.money.MoneyMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventHandler {

	@SubscribeEvent
	public static void onPlayerLogin(PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
			System.out.println(MoneyMod.AcctProvider.getBalance(event.getEntity().getUniqueID(), MoneyMod.playerAccounts));
		}
	}
}
