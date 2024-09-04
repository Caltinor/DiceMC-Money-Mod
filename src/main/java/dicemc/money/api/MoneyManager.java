package dicemc.money.api;

import java.util.UUID;

import dicemc.money.storage.MoneyWSD;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class MoneyManager implements IMoneyManager{
	//Singleton 
	private static final MoneyManager INSTANCE = new MoneyManager();
	private MoneyManager() {}
	public static MoneyManager get() {return INSTANCE;}
	
	@Override
	public double getBalance(ResourceLocation type, UUID id) {
		return MoneyWSD.get().getBalance(type, id);
	}
	@Override
	public boolean setBalance(ResourceLocation type, UUID id, double value) {
		return MoneyWSD.get().setBalance(type, id, value);
	}
	@Override
	public boolean changeBalance(ResourceLocation type, UUID id, double value) {
		return MoneyWSD.get().changeBalance(type, id, value);
	}
	@Override
	public boolean transferFunds(ResourceLocation fromType, UUID fromID, ResourceLocation toType, UUID toID,
			double value) {
		return MoneyWSD.get().transferFunds(fromType, fromID, toType, toID, value);
	}

}
