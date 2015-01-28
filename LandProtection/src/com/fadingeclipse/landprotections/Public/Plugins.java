package com.fadingeclipse.landprotections.Public;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Plugins {
	public static Essentials getEssentials() {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("Essentials"))
			return (Essentials)Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		else
			return null;
	}
	
	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public static WorldEditPlugin getWorldEdit() {
	    return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}
	
	public static Economy economy;
	public static void setupEconomy() {
		 if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
	            return;
	        }
		 
	        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
	            return;
	        }
	        
	        economy = rsp.getProvider();
	}
}
