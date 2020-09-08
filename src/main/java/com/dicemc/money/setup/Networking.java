package com.dicemc.money.setup;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.network.PacketAccountGuiUpdate;
import com.dicemc.money.network.PacketAccountToServer;
import com.dicemc.money.network.PacketAdminGuiUpdate;
import com.dicemc.money.network.PacketAdminToServer;
import com.dicemc.money.network.PacketOpenGui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
	private static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MoneyMod.MOD_ID, "net"),
			() -> "1.0", 
			s -> true, 
			s -> true);
	
	public static void registerMessages() {
		int ID = 0;
		INSTANCE.messageBuilder(PacketOpenGui.class, ID++)
				.encoder((packetOpenGui, packetBuffer) -> {})
				.decoder(buf -> new PacketOpenGui())
				.consumer(PacketOpenGui::handle)
				.add();
		INSTANCE.messageBuilder(PacketAccountToServer.class, ID++)
        		.encoder(PacketAccountToServer::toBytes)
        		.decoder(PacketAccountToServer::new)
        		.consumer(PacketAccountToServer::handle)
        		.add();
		INSTANCE.messageBuilder(PacketAccountGuiUpdate.class, ID++)
				.encoder(PacketAccountGuiUpdate::toBytes)
				.decoder(PacketAccountGuiUpdate::new)
				.consumer(PacketAccountGuiUpdate::handle)
				.add();
		INSTANCE.messageBuilder(PacketAdminGuiUpdate.class, ID++)
				.encoder(PacketAdminGuiUpdate::toBytes)
				.decoder(PacketAdminGuiUpdate::new)
				.consumer(PacketAdminGuiUpdate::handle)
				.add();
		INSTANCE.messageBuilder(PacketAdminToServer.class, ID++)
				.encoder(PacketAdminToServer::toBytes)
				.decoder(PacketAdminToServer::new)
				.consumer(PacketAdminToServer::handle)
				.add();
	}
	
	public static void sendToClient(Object packet, ServerPlayerEntity player) {
		INSTANCE.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}
}
