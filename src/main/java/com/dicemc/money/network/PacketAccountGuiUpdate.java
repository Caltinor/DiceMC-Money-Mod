package com.dicemc.money.network;

import java.util.function.Supplier;

import com.dicemc.money.client.GuiAccountManager;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAccountGuiUpdate {
	private final double balP;
	
	public PacketAccountGuiUpdate(PacketBuffer buf) { balP = buf.readDouble(); }
	
	public PacketAccountGuiUpdate(double value) { this.balP = value; }
	
	public void toBytes(PacketBuffer buf) { buf.writeDouble(balP); }
 	
	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> { GuiAccountManager.sync(balP); });
		return true;
	}
}
