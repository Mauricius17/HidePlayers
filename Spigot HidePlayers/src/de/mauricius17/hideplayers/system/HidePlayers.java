package de.mauricius17.hideplayers.system;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import de.mauricius17.hideplayers.mysql.MySQL;
import de.mauricius17.hideplayers.utils.Utils;

public class HidePlayers extends JavaPlugin  {

	private static HidePlayers instance;
	
	@Override
	public void onEnable() {
		instance = this;
		
		loadConfig();
		loadMessages();		
		registerEvents();
		
		Utils.setPrefix(ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("prefix")));

		new MySQL();
		
		if(MySQL.getSql().getBoolean("mysql")) {
			MySQL.connect();
			MySQL.createTable();
		} else {
			Bukkit.getConsoleSender().sendMessage("§4You have to complete your MySQL settings! Without these settings the server can not run sucessfuly!");
			Bukkit.shutdown();
		}		
	}
	
	@Override
	public void onDisable() {
		instance = null;
		MySQL.disconnect();
	}
	
	public static HidePlayers getInstance() {
		return instance;
	}
	
	private void registerEvents() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		try {
			for(ClassPath.ClassInfo classInfo : ClassPath.from(getClassLoader()).getTopLevelClasses("de.mauricius17.hideplayers.listener")) {
				Class<?> clazz = Class.forName(classInfo.getName());
				
				if(Listener.class.isAssignableFrom(clazz)) {
					pluginManager.registerEvents((Listener) clazz.newInstance(), this);
				}
			}
		} catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			Bukkit.getConsoleSender().sendMessage("§cThe Listeners could not be registered sucessfuly!");
			e.printStackTrace();
		}
		
	}
	
	private void loadConfig() {
		Utils.getConfig().options().header("In this file you can edit some settings");
		
		Utils.getConfig().addDefault("item_colorid_glass", 15);
		Utils.getConfig().addDefault("item_colorid_hidden", 1);
		Utils.getConfig().addDefault("item_colorid_visable", 10);
		Utils.getConfig().addDefault("item_colorid_members", 5);
		Utils.getConfig().addDefault("item_colorid_vip", 4);
		Utils.getConfig().addDefault("item_colorid_youtuber", 10);
		Utils.getConfig().addDefault("item_colorid_architect", 13);
		Utils.getConfig().addDefault("item_colorid_presenter", 6);
		Utils.getConfig().addDefault("item_colorid_developer", 3);
		Utils.getConfig().addDefault("item_colorid_administrator", 14);
		
		Utils.getConfig().addDefault("inventory_slot", 1);
		Utils.getConfig().options().copyDefaults(true);
		
		try {
			Utils.getConfig().save(Utils.getConfigFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMessages() {
		Utils.getMessages().options().header("In this file you can edit some messages!");
		
		Utils.getMessages().addDefault("prefix", "&8[&5HidePlayers&8] ");
		Utils.getMessages().addDefault("players_successfuly_hidden", "&cThe group [COLOR] [GROUP] &cis now hidden!");
		Utils.getMessages().addDefault("players_successfuly_visable", "&aThe group [COLOR] [GROUP] &ais now visable!");
		Utils.getMessages().addDefault("item", "&6Item to hide and show players");
		Utils.getMessages().addDefault("inventory", "&6Hide/Show Players");
		Utils.getMessages().addDefault("inventory_visable", "&aPlayers are visable");
		Utils.getMessages().addDefault("inventory_hidden", "&cPlayers are hidden");
		Utils.getMessages().addDefault("inventory_group_member", "&aMembers");
		Utils.getMessages().addDefault("inventory_group_vip", "&6VIPs");
		Utils.getMessages().addDefault("inventory_group_youtuber", "&5Youtubers");
		Utils.getMessages().addDefault("inventory_group_architect", "&2Architects");
		Utils.getMessages().addDefault("inventory_group_presenter", "&cPresenters");
		Utils.getMessages().addDefault("inventory_group_developer", "&bDevelopers");
		Utils.getMessages().addDefault("inventory_group_administrator", "&4Administrators");
		
		Utils.getMessages().options().copyDefaults(true);
		
		try {
			Utils.getMessages().save(Utils.getMessageFile());
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage("§cThe messages.yml could not be saved successfuly!");
			e.printStackTrace();
		}
	}
}