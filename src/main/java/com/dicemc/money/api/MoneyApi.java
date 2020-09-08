package com.dicemc.money.api;

import java.util.UUID;

import com.dicemc.money.core.IMoneyApi;

import net.minecraft.util.ResourceLocation;

public class MoneyApi implements IAccountProvider{
	private static IMoneyApi API = null;
	
	@Override
	public void setAPI(IMoneyApi api) {
		if (API == null) API = api;
	}
	
	@Override
	public IMoneyApi getAPI() {return API;}
	
	@Override
	public double getBalance(UUID owner, ResourceLocation ownerType) {
		return API.getBalance(owner, ownerType);
	}
	
	@Override
	public boolean setBalance(UUID owner, ResourceLocation ownerType, double value) {
		return API.setBalance(owner, ownerType, value);
	}
	
	@Override
	public boolean changeBalance(UUID owner, ResourceLocation ownerType, double value) {
		return API.changeBalance(owner, ownerType, value);
	}
	
	@Override
	public boolean transferFunds(UUID ownerFrom, ResourceLocation ownerFromType, UUID ownerTo, ResourceLocation owernToType, double value) {
		return API.transferFunds(ownerFrom, ownerFromType, ownerTo, owernToType, value);
	}	
}
