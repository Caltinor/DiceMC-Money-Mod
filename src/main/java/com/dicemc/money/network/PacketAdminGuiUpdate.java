package com.dicemc.money.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.dicemc.money.client.GuiAdmin;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAdminGuiUpdate {
	private Map<String, Double> masterList = new HashMap<String, Double>();
	
	public PacketAdminGuiUpdate(PacketBuffer buf) {
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			masterList.put(buf.readString(), buf.readDouble());
		}
	}
	
	public PacketAdminGuiUpdate(Map<String, Double> masterList) { this.masterList = masterList; }
	
	public void toBytes(PacketBuffer buf) {
		buf.writeInt(masterList.size());
		for (Map.Entry<String, Double> map : masterList.entrySet()) {
			buf.writeString(map.getKey());
			buf.writeDouble(map.getValue());			
		}
	}
 	
	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			GuiAdmin.sync(masterList);
		});
		return true;
	}
}
