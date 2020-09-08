package com.dicemc.money.network;

import java.util.function.Supplier;

import com.dicemc.money.client.GuiAdmin;

import net.minecraftforge.fml.network.NetworkEvent;

public class PacketOpenGui {
	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(GuiAdmin::open);
		return true;
	}
}
