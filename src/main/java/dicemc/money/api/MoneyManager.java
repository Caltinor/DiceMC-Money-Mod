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
	private ServerLevel world;
	
	public void setWorld(ServerLevel world) {this.world = world;}
	
	@Override
	public double getBalance(ResourceLocation type, UUID id) {
		return MoneyWSD.get(world).getBalance(type, id);
	}
	@Override
	public boolean setBalance(ResourceLocation type, UUID id, double value) {
		return MoneyWSD.get(world).setBalance(type, id, value);
	}
	@Override
	public boolean changeBalance(ResourceLocation type, UUID id, double value) {
		return MoneyWSD.get(world).changeBalance(type, id, value);
	}
	@Override
	public boolean transferFunds(ResourceLocation fromType, UUID fromID, ResourceLocation toType, UUID toID,
			double value) {
		return MoneyWSD.get(world).transferFunds(fromType, fromID, toType, toID, value);
	}

}
