package com.dicemc.money.network;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Networking;

import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAdminToServer {
	private final PkType action;
	private final double value;
	private final String name;
	
	public enum PkType {
		SET((packet, ctx) -> {
			Map<String, Double> map = new HashMap<String, Double>();
			UUID target = ctx.get().getSender().getServer().getPlayerProfileCache().getGameProfileForUsername(packet.name).getId();
			MoneyMod.AcctProvider.setBalance(target, MoneyMod.playerAccounts, packet.value);
			return getMasterList(ctx.get().getSender().getServer(), MoneyMod.playerAccounts);
		}),
		ADD((packet, ctx) -> {
			UUID target = ctx.get().getSender().getServer().getPlayerProfileCache().getGameProfileForUsername(packet.name).getId();
			MoneyMod.AcctProvider.changeBalance(target, MoneyMod.playerAccounts, packet.value);
			return getMasterList(ctx.get().getSender().getServer(), MoneyMod.playerAccounts);
		}),
		SUB((packet, ctx) -> {
			UUID target = ctx.get().getSender().getServer().getPlayerProfileCache().getGameProfileForUsername(packet.name).getId();
			MoneyMod.AcctProvider.changeBalance(target, MoneyMod.playerAccounts, -1 * packet.value);
			return getMasterList(ctx.get().getSender().getServer(), MoneyMod.playerAccounts);
		}),
		SYNC((packet, ctx) -> getMasterList(ctx.get().getSender().getServer(), MoneyMod.playerAccounts));
		
		public final BiFunction<PacketAdminToServer, Supplier<NetworkEvent.Context>, Map<String, Double>> packetHandler;
		
		PkType(BiFunction<PacketAdminToServer, Supplier<NetworkEvent.Context>, Map<String, Double>> packetHandler) { this.packetHandler = packetHandler;}
	}
	
	public PacketAdminToServer(PacketBuffer buf) {
		action = PkType.values()[buf.readVarInt()];
		value = buf.readDouble();
		name = buf.readString(32767);
	}
	
	public PacketAdminToServer(PkType type, double value, String name) {
		this.action = type;
		this.value = value;
		this.name = name;
	}
	
	public void toBytes(PacketBuffer buf) {
		buf.writeVarInt(action.ordinal());
		buf.writeDouble(value);
		buf.writeString(name);
	}
 	
	public boolean handle(Supplier<NetworkEvent.Context> ctx) { //consumer method
		ctx.get().enqueueWork(() -> {
			Networking.sendToClient(new PacketAdminGuiUpdate(this.action.packetHandler.apply(this, ctx)), ctx.get().getSender());
		});
		return true;
	}
	
	private static Map<String, Double> getMasterList(MinecraftServer server, ResourceLocation type) {
		String sql = "SELECT * FROM tblAccounts WHERE type=?";
		PreparedStatement st = null;
		try {
			st = MoneyMod.dbm.con.prepareStatement(sql);
			st.setString(1, MoneyMod.playerAccounts.toString());
		} catch (SQLException e1) {e1.printStackTrace();}
		ResultSet rs = MoneyMod.dbm.executeSELECT(st);
		Map<String, Double> map = new HashMap<String, Double>();
		try {
			if (!rs.isBeforeFirst()) return map;
			while (rs.next()) {
				String name = server.getPlayerProfileCache().getProfileByUUID(rs.getObject("Owner", UUID.class)).getName();
				map.put(name, rs.getDouble("balance"));
			}
		} catch (SQLException e1) {e1.printStackTrace();}
		return map;
	}
}
