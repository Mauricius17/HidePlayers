package de.mauricius17.hideplayers.utils;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Utils {
	
	private static File messageFile = new File("plugins/HidePlayers", "messages.yml");
	private static FileConfiguration messages = YamlConfiguration.loadConfiguration(messageFile);
	
	private static File configFile = new File("plugins/HidePlayers", "config.yml");
	private static FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	
	private static String prefix = "";
	
	public static void setPrefix(String prefix) {
		Utils.prefix = prefix;
	}
	
	public static FileConfiguration getConfig() {
		return config;
	}
	
	public static File getConfigFile() {
		return configFile;
	}
	
	public static String getPrefix() {
		return prefix;
	}	
	
	public static File getMessageFile() {
		return messageFile;
	}
	
	public static FileConfiguration getMessages() {
		return messages;
	}
}