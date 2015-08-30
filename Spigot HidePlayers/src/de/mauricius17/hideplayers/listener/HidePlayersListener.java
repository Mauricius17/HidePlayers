package de.mauricius17.hideplayers.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.mauricius17.hideplayers.mysql.MySQL_HidePlayers;
import de.mauricius17.hideplayers.system.HidePlayers;
import de.mauricius17.hideplayers.utils.Items;
import de.mauricius17.hideplayers.utils.Utils;

public class HidePlayersListener implements Listener {

	String NAVIGATOR = ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.name"));
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getItem() != null && e.getItem().hasItemMeta()) {
				if(e.getItem().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("item.name")))) { 
					openInventory(p);
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if(e.getInventory().getName().equalsIgnoreCase(NAVIGATOR)) {
				e.setCancelled(true);
				
				if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getType().equals(Material.STAINED_CLAY)) {
					for(int i = 0; i < Groups.values().length; i++) {
						if(e.getCurrentItem().getItemMeta().getDisplayName().equals(Groups.values()[i].getDisplayname())) {
							if(isHidden(p, Groups.values()[i])) {
								Groups.values()[i].getHidden().remove(p.getUniqueId());
								showPlayers(p, Groups.values()[i]);
								p.sendMessage(Utils.getPrefix() + ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("players.successfuly.visable")).replace("[COLOR]", Groups.values()[i].getColor()).replace("[GROUP]", Groups.values()[i].getDisplayname()));
								p.closeInventory();
								MySQL_HidePlayers.showGroup(p.getUniqueId().toString(), Groups.values()[i]);
							} else {
								Groups.values()[i].getHidden().add(p.getUniqueId());
								hidePlayers(p, Groups.values()[i]);
								p.sendMessage(Utils.getPrefix() + ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("players.successfuly.hidden")).replace("[COLOR]", Groups.values()[i].getColor()).replace("[GROUP]", Groups.values()[i].getDisplayname()));
								p.closeInventory();
								MySQL_HidePlayers.hideGroup(p.getUniqueId().toString(), Groups.values()[i]);
							}
							
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 1F, 1F);
						}
					}
				}				
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		for(int i = 0; i < Groups.values().length; i++) {
			if(p.hasPermission(Groups.values()[i].getPermission())) {
				Groups.values()[i].getPlayers().add(p.getUniqueId());
			}
		}
		
		for(Player players : Bukkit.getOnlinePlayers()) {
			if(players != p) {
				for(int i = 0; i < Groups.values().length; i++) {
					if(isHidden(players, Groups.values()[i])) {
						if(p.hasPermission(Groups.values()[i].getPermission())) {
							players.hidePlayer(p);
						}
					}
				}	
			}
		}
	
		MySQL_HidePlayers.getHiddenGroups(p.getUniqueId().toString(), new Consumer<String>() {
			
			@Override
			public void accept(String result) {
				if(!result.equals("wrong")) {
						String[] res = result.split(";");
						
						for(int i = 0; i < Groups.values().length; i++) {
							for(int r = 0; r < res.length; r++) {
								if(res[r].equals(Groups.values()[i].toString())) {
									if(!isHidden(p, Groups.values()[i])) {
										Groups.values()[i].getHidden().add(p.getUniqueId());
										hidePlayers(p, Groups.values()[i]);	
									}
								}
							}
						}
				}
			}
		});
		
		ItemStack item = Items.getItemStack(getMaterial(Utils.getConfig().getString("inventory.item.type")), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("item.name")), 1, (byte) 0);
		
		try {
			p.getInventory().setItem(Utils.getConfig().getInt("inventory.slot"), item);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		
		for(int i = 0; i < Groups.values().length; i++) {
			if(Groups.values()[i].getPlayers().contains(p.getUniqueId())) {
				Groups.values()[i].getPlayers().remove(p.getUniqueId());
			}
			
			if(Groups.values()[i].getHidden().contains(p.getUniqueId())) {
				Groups.values()[i].getHidden().remove(p.getUniqueId());
			}
		}
	}
	
	private static Material getMaterial(String material) {
		for(Material mat : Material.values()) {
			if(mat.toString().endsWith(material)) {
				return mat;
			}
		}
		
		return null;
	}
	
	private void openInventory(Player p) {
		Inventory inventory = Bukkit.createInventory(null, 36, NAVIGATOR);
		
		try {
			ItemStack glass = Items.getItemStack(Material.STAINED_GLASS_PANE, " ", 1, Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.glass")));
			
			ItemStack hidden = Items.getItemStack(Material.INK_SACK, ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.hidden")), 1, Byte.valueOf(Utils.getConfig().getString("item.colorid.hidden")));
			ItemStack visible = Items.getItemStack(Material.INK_SACK, ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.visable")), 1,  Byte.valueOf(Utils.getConfig().getString("item.colorid.visable")));
			
			ItemStack members = Items.getItemStack(Material.STAINED_CLAY, Groups.MEMBERS.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.members")));
			ItemStack vips = Items.getItemStack(Material.STAINED_CLAY, Groups.VIP.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.vip")));
			ItemStack youtuber = Items.getItemStack(Material.STAINED_CLAY, Groups.YOUTUBER.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.youtuber")));
			ItemStack architects = Items.getItemStack(Material.STAINED_CLAY, Groups.ARCHITECTS.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.architect")));
			ItemStack presenters = Items.getItemStack(Material.STAINED_CLAY, Groups.PRESENTERS.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.presenter")));
			ItemStack developer = Items.getItemStack(Material.STAINED_CLAY, Groups.DEVELOPER.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.developer")));
			ItemStack administrator = Items.getItemStack(Material.STAINED_CLAY, Groups.ADMINISTRATOR.getDisplayname(), 1,  Byte.valueOf((byte) Utils.getConfig().getInt("item.colorid.administrator")));	
			
			for(int i = 0; i < inventory.getSize(); i++) {
				inventory.setItem(i, glass);
			}
			
			inventory.setItem(Groups.MEMBERS.getSlot(), members);
			inventory.setItem(Groups.VIP.getSlot(), vips);
			inventory.setItem(Groups.YOUTUBER.getSlot(), youtuber);
			inventory.setItem(Groups.ARCHITECTS.getSlot(), architects);
			inventory.setItem(Groups.PRESENTERS.getSlot(), presenters);
			inventory.setItem(Groups.DEVELOPER.getSlot(), developer);
			inventory.setItem(Groups.ADMINISTRATOR.getSlot(), administrator);
		
			for(int i = 0; i < Groups.values().length; i++) {
				if(isHidden(p, Groups.values()[i])) {
					inventory.setItem((Groups.values()[i].getSlot() + 9), hidden);
				} else {
					inventory.setItem((Groups.values()[i].getSlot() + 9), visible);
				}
			}
		} catch (NumberFormatException e) {
			Bukkit.getConsoleSender().sendMessage("§cThe Server got an NumerFormatException! Please choose right numbers");
			e.printStackTrace();
		}
		
		p.openInventory(inventory);
	}
	
	private boolean isHidden(Player p, Groups group) {
		return group.getHidden().contains(p.getUniqueId());
	}
	
	private void hidePlayers(Player p, Groups group) {
		for(UUID uuids : group.getPlayers()) {
			Player player = Bukkit.getPlayer(uuids);
			
			if(player != null) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(HidePlayers.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						p.hidePlayer(player);
					}
				});
			}
		}
	}
	
	private void showPlayers(Player p, Groups group) {
		for(UUID uuids : group.getPlayers()) {
			Player player = Bukkit.getPlayer(uuids);
			
			if(player != null) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(HidePlayers.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						p.showPlayer(player);				
					}
				});
			}
		}
	}
	
	public enum Groups {
		MEMBERS(new ArrayList<UUID>(), "member", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.member")), 10, "§a"),
		VIP(new ArrayList<UUID>(), "vip", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.vip")), 11, "§6"),
		YOUTUBER(new ArrayList<UUID>(), "youtuber", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.youtuber")), 12, "§5"),
		ARCHITECTS(new ArrayList<UUID>(), "architect", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.architect")), 13, "§2"),
		PRESENTERS(new ArrayList<UUID>(), "presenter", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.presenter")), 14, "§c"),
		DEVELOPER(new ArrayList<UUID>(), "developer", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.developer")), 15, "§b"),
		ADMINISTRATOR(new ArrayList<UUID>(), "administrator", new ArrayList<UUID>(), ChatColor.translateAlternateColorCodes('&', Utils.getMessages().getString("inventory.group.administrator")), 16, "§4");
		
		List<UUID> players;
		List<UUID> hidden;
		String permission;
		String displayname;
		int slot;
		String color;
		
		private Groups(List<UUID> players, String permission, List<UUID> hidden, String displayname, int slot, String color) {
			this.players = players;
			this.permission = permission;
			this.hidden = hidden;
			this.displayname = displayname;
			this.slot = slot;
			this.color = color;
		}
		
		public String getColor() {
			return color;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public List<UUID> getHidden() {
			return hidden;
		}
		
		public String getDisplayname() {
			return displayname;
		}
		
		public String getPermission() {
			return permission;
		}
		
		public List<UUID> getPlayers() {
			return players;
		}
	}
}