package com.dicemc.money.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.dicemc.money.MoneyMod;
import com.dicemc.money.setup.Config;

import net.minecraft.util.ResourceLocation;

public class AccountManager implements IMoneyApi {

	@Override
	public double getBalance(UUID owner, ResourceLocation ownerType) {
		String sql = "SELECT * FROM tblAccounts WHERE Owner=? AND type=?";
		PreparedStatement st = null;
		try {
			st = MoneyMod.dbm.con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, ownerType.toString());
		} catch (SQLException e1) {e1.printStackTrace();}
		

		ResultSet rs =MoneyMod.dbm.executeSELECT(st);
		try {
			if (!rs.isBeforeFirst()) {addAccount(owner, ownerType);}
			if (rs.next()) {return rs.getDouble("balance");			
		}
		} catch (SQLException e) {e.printStackTrace();}
		return 0;
	}

	@Override
	public boolean setBalance(UUID owner, ResourceLocation ownerType, double value) {
		String sql = "UPDATE tblAccounts SET balance=? WHERE Owner=? AND type=?";
		PreparedStatement st = null;
		try {
			st = MoneyMod.dbm.con.prepareStatement(sql);
			st.setObject(2, owner);
			st.setString(3, ownerType.toString());
			st.setDouble(1, value);
		} catch (SQLException e1) {e1.printStackTrace();}
		int out = MoneyMod.dbm.executeUPDATE(st);
		
		return out > 0;
	}

	@Override
	public boolean changeBalance(UUID owner, ResourceLocation ownerType, double value) {
		String sql = "SELECT * FROM tblAccounts WHERE Owner=? AND type=?";
		PreparedStatement st = null;
		try {
			st = MoneyMod.dbm.con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, ownerType.toString());
		} catch (SQLException e1) {e1.printStackTrace(); return false;}
		ResultSet rs = MoneyMod.dbm.executeSELECT(st);
		double currentBal = 0d;
		try {
			if (!rs.isBeforeFirst()) return false;
			if (rs.next()) {
				currentBal = rs.getDouble("balance");
			}
		} catch (SQLException e) {e.printStackTrace(); return false;}
		currentBal += value;
		setBalance(owner, ownerType, currentBal);
		return true;
	}

	@Override
	public boolean transferFunds(UUID ownerFrom, ResourceLocation ownerFromType, UUID ownerTo, ResourceLocation ownerToType, double value) {
		double from = getBalance(ownerFrom, ownerFromType);
		if (from >= value) {
			int check = 0;
			check += changeBalance(ownerFrom, ownerFromType, (-1 * value)) ? 1: 0;
			check += changeBalance(ownerTo, ownerToType, value) ? 1 : 0;
			return check == 2;
		}
		return false;
	}
	
	private double addAccount(UUID owner, ResourceLocation type) {
		String sql = "INSERT INTO tblAccounts (Owner, type, balance) " +
				"VALUES (? ,? ,?)";
		PreparedStatement st = null;
		try {
			st = MoneyMod.dbm.con.prepareStatement(sql);
			st.setObject(1, owner);
			st.setString(2, type.toString());
			st.setDouble(3, Config.STARTING_FUNDS.get());
		} catch (SQLException e1) {e1.printStackTrace();}
		
		MoneyMod.dbm.executeUPDATE(st);
		return Config.STARTING_FUNDS.get();
	}

}
