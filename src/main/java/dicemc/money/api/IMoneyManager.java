package dicemc.money.api;

import java.util.UUID;

import net.minecraft.util.ResourceLocation;

public interface IMoneyManager {
	double getBalance(ResourceLocation type, UUID id);
	boolean setBalance(ResourceLocation type, UUID id, double value);
	boolean changeBalance(ResourceLocation type, UUID id, double value);
	boolean transferFunds(ResourceLocation fromType, UUID fromID, ResourceLocation toType, UUID toID, double value);
}
