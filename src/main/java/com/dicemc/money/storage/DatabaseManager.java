package com.dicemc.money.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dicemc.money.setup.Config;

public class DatabaseManager {
	public Connection con;
	
	public DatabaseManager(String saveName, String urlIn) {
		String port = Config.DB_PORT.get();
		String name = saveName + Config.DB_NAME.get();
		String service = Config.DB_SERVICE.get();
		String host = "jdbc:"+service+":" + urlIn + port + name;;
		String user = Config.DB_USER.get();
		String pass = Config.DB_PASS.get();

		try {
			System.out.println("Attempting DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			Statement stmt = con.createStatement();
			System.out.println("DB Connection Successful");
			stmt.execute("CREATE TABLE IF NOT EXISTS tblAccounts (" +
					"ID INT AUTO_INCREMENT PRIMARY KEY, " +
					"Owner UUID, " +
					"type VARCHAR, " +
					"balance DOUBLE);");
		} catch (SQLException e) {e.printStackTrace();}	
		
	}
	
	public ResultSet executeSELECT(PreparedStatement sql) {
		try {return sql.executeQuery();
		} catch (SQLException e) {e.printStackTrace();}
		return null;
	}
	
	public int executeUPDATE(PreparedStatement sql) {
		try {return sql.executeUpdate();
		} catch (SQLException e) {e.printStackTrace();}
		return 0;
	}
}
