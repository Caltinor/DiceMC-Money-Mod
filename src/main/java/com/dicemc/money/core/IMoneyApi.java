package com.dicemc.money.core;

import java.util.UUID;

import net.minecraft.util.ResourceLocation;

public interface IMoneyApi {

	double getBalance(UUID owner, ResourceLocation ownerType);
	
	boolean setBalance(UUID owner, ResourceLocation ownerType, double value);
	
	boolean changeBalance(UUID owner, ResourceLocation ownerType, double value);
	
	boolean transferFunds(UUID ownerFrom, ResourceLocation ownerFromType, UUID ownerTo, ResourceLocation owernToType, double value);
}
