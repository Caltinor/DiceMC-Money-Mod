package dicemc.money.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dicemc.money.MoneyMod;
import dicemc.money.api.IMoneyManager;
import dicemc.money.setup.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class MoneyWSD extends SavedData implements IMoneyManager{
	private static final String DATA_NAME = MoneyMod.MOD_ID + "_data";

	public MoneyWSD(CompoundTag nbt) {this.load(nbt);}
	public MoneyWSD() {}
	
	private Map<ResourceLocation, Map<UUID, Double>> accounts = new HashMap<>();
	
	public Map<UUID, Double> getAccountMap(ResourceLocation res) {return accounts.getOrDefault(res, new HashMap<>());}
	
	@Override
	public double getBalance(ResourceLocation type, UUID owner) {
		accountChecker(type, owner);
		return accounts.getOrDefault(type, new HashMap<>()).get(owner);
	}
	
	@Override
	public boolean setBalance(ResourceLocation type, UUID id, double value) {
		if (type != null && accounts.containsKey(type)) {
			if (id != null) {				
				accounts.get(type).put(id, value);
				this.setDirty();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean changeBalance(ResourceLocation type, UUID id, double value) {
		if (type == null || id == null) return false;
		double current = getBalance(type, id);
		double future = current + value;
		return setBalance(type, id, future);
	}
	
	@Override
	public boolean transferFunds(ResourceLocation fromType, UUID fromID, ResourceLocation toType, UUID toID, double value) {
		if (fromType == null || fromID == null || toType == null || toID == null) return false;
		double funds = Math.abs(value);
		double fromBal = getBalance(fromType, fromID);
		if (fromBal < funds) return false;
		if (changeBalance(fromType, fromID, -funds) && changeBalance(toType, toID, funds)) { 
			this.setDirty();
			return true;
		}
		else 
			return false;
	}
	
	public void accountChecker(ResourceLocation type, UUID owner) {
		if (type != null && !accounts.containsKey(type)) {
			accounts.put(type, new HashMap<>());
			this.setDirty();
		}
		if (owner != null && !accounts.get(type).containsKey(owner)) {
			accounts.get(type).put(owner, Config.STARTING_FUNDS.get());
			this.setDirty();
		}
	}

	public void load(CompoundTag nbt) {
		ListTag baseList = nbt.getList("types", NBT.TAG_COMPOUND);
		for (int b = 0; b < baseList.size(); b++) {
			CompoundTag entry = baseList.getCompound(b);
			ResourceLocation res = new ResourceLocation(entry.getString("type"));
			Map<UUID, Double> data = new HashMap<>();
			ListTag list = entry.getList("data", NBT.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag snbt = list.getCompound(i);
				UUID id = snbt.getUUID("id");
				double balance = snbt.getDouble("balance");
				data.put(id, balance);
			}
			accounts.put(res, data);
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		ListTag baseList = new ListTag();
		for (Map.Entry<ResourceLocation, Map<UUID, Double>> base : accounts.entrySet()) {
			CompoundTag entry = new CompoundTag();
			ListTag list = new ListTag();
			entry.putString("type", base.getKey().toString());
			for (Map.Entry<UUID, Double> data : base.getValue().entrySet()) {
				CompoundTag dataNBT = new CompoundTag();
				dataNBT.putUUID("id", data.getKey());
				dataNBT.putDouble("balance", data.getValue());
				list.add(dataNBT);
			}
			entry.put("data", list);
			baseList.add(entry);
		}
		nbt.put("types", baseList);
		return nbt;
	}
	
	public static MoneyWSD get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(MoneyWSD::new, MoneyWSD::new, DATA_NAME);
	}
}
