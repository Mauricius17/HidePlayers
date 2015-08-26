package de.mauricius17.hideplayers.mysql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MySQL {

	private static Connection connection;
	private static String host;
	private static int port;
	private static String database;
	private static String username;
	private static String password;
	private static ExecutorService executor;
	
	private static File MySQLFile = new File("plugins/HidePlayers", "mysql.yml");
	private static FileConfiguration sql = YamlConfiguration.loadConfiguration(MySQLFile);

	private static String TABLE, HIDDEN = "hiddenGroups";
	
	static {
		executor = Executors.newCachedThreadPool();
	}

	public static Connection getConnection() {
		return connection;
	}
	
	public static FileConfiguration getSql() {
		return sql;
	}
	
	public static String getTABLE() {
		return TABLE;
	}
	
	public static String getHIDDEN() {
		return HIDDEN;
	}
	
	public static ExecutorService getExecutor() {
		return executor;
	}
	
	public MySQL() {

		sql.addDefault("mysql", false);
		sql.addDefault("allowPassword", true);
		sql.addDefault("host", "host");
		sql.addDefault("port", Integer.valueOf(3306));
		sql.addDefault("database", "Database");
		sql.addDefault("username", "Username");
		sql.addDefault("password", "Password");
		sql.addDefault("table_name", "hide_players");

		sql.options().copyDefaults(true);

		try {
			sql.save(MySQLFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		host = sql.getString("host");
		port = sql.getInt("port");
		database = sql.getString("database");
		username = sql.getString("username");
		TABLE = sql.getString("table_name");
		
		if (sql.getBoolean("allowPassword"))
			password = sql.getString("password");
		else
			password = "";
	}
	
	public static void connect() {
		if(!(isConnected())) {
			try {
				Bukkit.getConsoleSender().sendMessage("§aMySQL is connecting ...");
				connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
				Bukkit.getConsoleSender().sendMessage("§aMySQL connected!");
			} catch(SQLException ex) {
				Bukkit.getConsoleSender().sendMessage("§cMySQL could not connect!");
				ex.printStackTrace();
			}
		}
	}
	
	public static boolean isConnected() {
		try {
			if(connection != null && connection.isValid(1)) {
				return true;
			}
		} catch(SQLException ex) {
			Bukkit.getConsoleSender().sendMessage("§cMySQL could not check for connection!");
			ex.printStackTrace();
		}
		
		return false;
	}
	
	public static void disconnect() {
		if(isConnected()) {
			try {
				Bukkit.getConsoleSender().sendMessage("§aMySQL connection is closing ...");
				connection.close();
				Bukkit.getConsoleSender().sendMessage("§aMySQL connection closed!");
			} catch (SQLException e) {
				Bukkit.getConsoleSender().sendMessage("§cMySQL could not close the connection!");
				e.printStackTrace();
			}
		}
	}
	
	public static void createTable() {
		try {
			PreparedStatement stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + TABLE + "(playeruuid text, " + HIDDEN + " text)");
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException ex) {
			Bukkit.getConsoleSender().sendMessage("§cMySQL could not create the Table!");
			ex.printStackTrace();
		}
	}
	
}
