package de.mauricius17.hideplayers.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Items {

	public static ItemStack getItemStack(Material mat, String displayname, int amount, byte subID) {
		ItemStack item = new ItemStack(mat, amount, (byte) subID);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		item.setItemMeta(meta);
		
		return item;
	}
	
}
