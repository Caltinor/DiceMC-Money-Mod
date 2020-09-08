package com.dicemc.money.api;

import java.util.UUID;

import com.dicemc.money.core.IMoneyApi;

import net.minecraft.util.ResourceLocation;

public interface IAccountProvider {
	IMoneyApi API = null;
	void setAPI(IMoneyApi api);	
	IMoneyApi getAPI();
	double getBalance(UUID owner, ResourceLocation ownerType);
	boolean setBalance(UUID owner, ResourceLocation ownerType, double value);
	boolean changeBalance(UUID owner, ResourceLocation ownerType, double value);
	boolean transferFunds(UUID ownerFrom, ResourceLocation ownerFromType, UUID ownerTo, ResourceLocation owernToType, double value);
}
