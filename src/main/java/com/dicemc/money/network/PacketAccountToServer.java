package com.dicemc.money.network;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Networking;
import com.dicemc.money.setup.Registration;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PacketAccountToServer {
	private final PkType action;
	private final double value;
	private final int count;
	private final String name;
	
	public enum PkType {
		DEPOSIT ((packet, ctx) -> {
			MoneyMod.AcctProvider.changeBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts, packet.value);
			for (int i = 0; i < ctx.get().getSender().openContainer.getInventory().size(); i++) {
				ctx.get().getSender().openContainer.getInventory().get(i).setCount(0);
			}
			ctx.get().getSender().openContainer.detectAndSendChanges();
			return MoneyMod.AcctProvider.getBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts);
		}),
		WITHDRAW ((packet, ctx) -> {
			double bal = MoneyMod.AcctProvider.getBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts);
			if (bal >= (packet.value * packet.count)) {
				MoneyMod.AcctProvider.changeBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts, -1 * packet.value * packet.count);
				ItemStack bag = new ItemStack(ForgeRegistries.ITEMS.getValue(Registration.MONEYBAG.getId()));
				bag.setCount(packet.count);
				CompoundNBT nbt = new CompoundNBT();
				nbt.putDouble("value", packet.value);
				bag.setTag(nbt);
				ctx.get().getSender().addItemStackToInventory(bag);
			}			
			return MoneyMod.AcctProvider.getBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts);
		}),
		TRANSFER ((packet, ctx) -> {
			double bal = MoneyMod.AcctProvider.getBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts);
			if (bal >= packet.value) {
				UUID recipient = ctx.get().getSender().getServer().getPlayerProfileCache().getGameProfileForUsername(packet.name).getId();
				MoneyMod.AcctProvider.changeBalance(recipient, MoneyMod.playerAccounts, packet.value);
				MoneyMod.AcctProvider.changeBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts, -1 * packet.value);
			}
			return MoneyMod.AcctProvider.getBalance(ctx.get().getSender().getUniqueID(), MoneyMod.playerAccounts); 
		});
		
		public final BiFunction<PacketAccountToServer, Supplier<NetworkEvent.Context>, Double> packetHandler;
		
		PkType(BiFunction<PacketAccountToServer, Supplier<NetworkEvent.Context>, Double> packetHandler) { this.packetHandler = packetHandler;}
	}
	
	public PacketAccountToServer(PacketBuffer buf) {
		action = PkType.values()[buf.readVarInt()];
		value = buf.readDouble();
		count = buf.readInt();
		name = buf.readString(32767);		
	}
	
	public PacketAccountToServer(PkType action, double value, String name) {
		this.action = action;
		this.value = value;
		this.name = name;
		this.count = 1;
	}
	
	public PacketAccountToServer(PkType action, double value, int count) {
		this.action = action;
		this.value = value;
		this.name = "";
		this.count = count;
	}
	
	public PacketAccountToServer(PkType action, double value) {
		this.action = action;
		this.value = value;
		this.name = "";
		this.count = 1;
	}
	
	public void toBytes(PacketBuffer buf) {
		System.out.println("Packets!!");
		buf.writeVarInt(action.ordinal());
		buf.writeDouble(value);
		buf.writeInt(count);
		buf.writeString(name);
	}
 	
	public boolean handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Networking.sendToClient(new PacketAccountGuiUpdate(this.action.packetHandler.apply(this, ctx)), ctx.get().getSender());
		});
		return true;
	}
}
