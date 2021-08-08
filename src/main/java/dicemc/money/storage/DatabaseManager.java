package dicemc.money.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

public class DatabaseManager {
	public Connection con;
	public MinecraftServer server;
	public static UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public void setServer(MinecraftServer server) {this.server = server;}
	
	public DatabaseManager(String saveName, String urlIn) {
		String port = "";
		String name = saveName + "transaction_history";
		String service = "h2";
		String host = "jdbc:"+service+":" + urlIn + port + name;;
		String user = "sa";
		String pass = "";

		try {
			System.out.println("Attempting DB Connection");
			con = DriverManager.getConnection(host, user, pass);
			Statement stmt = con.createStatement();
			System.out.println("DB Connection Successful");
			stmt.execute("CREATE TABLE IF NOT EXISTS History (" +
					"ID INT AUTO_INCREMENT PRIMARY KEY, " +
					"DTG BIGINT NOT NULL" +
					"FROM_ID UUID NOT NULL, " +
					"FROM_TYPE VARCHAR NOT NULL, " +
					"FROM_NAME VARCHAR NOT NULL, " +
					"TO_ID UUID NOT NULL, " +
					"TO_TYPE VARCHAR NOT NULL, " +
					"TO_NAME VARCHAR NOT NULL, " +
					"PRICE DOUBLE NOT NULL, " +
					"ITEM VARCHAR NOT NULL);");
		} catch (SQLException e) {e.printStackTrace();}	
		
	}
	
	public void postEntry(long DTG, UUID fromID, ResourceLocation fromType, String fromName, UUID toID, ResourceLocation toType, String toName, double price, String item) {
		String sql = "INSERT INTO History (DTG, FROM_ID, FROM_TYPE, FROM_NAME, TO_ID, TO_TYPE, TO_NAME, PRICE, ITEM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(sql);
			st.setLong(1, DTG);
			st.setString(2, fromID.toString());
			st.setString(3, fromType.toString());
			st.setString(4, fromName);
			st.setString(5, toID.toString());
			st.setString(6, toType.toString());
			st.setString(7, toName);
			st.setDouble(8, price);
			st.setString(9, item);
		} catch (SQLException e) {e.printStackTrace();}
		executeUPDATE(st);
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
