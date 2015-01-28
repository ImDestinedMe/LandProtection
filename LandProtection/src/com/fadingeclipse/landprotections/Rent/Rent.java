package com.fadingeclipse.landprotections.Rent;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.fadingeclipse.landprotections.Public.Plugins;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

//import com.fadingeclipse.landprotections.Public.Plugins;

public class Rent {
	public File rentingYML;
	public Renters rentingManager;
	
	WorldGuardPlugin worldGuard = Plugins.getWorldGuard();
	
	public Rent(File config) {
		this.rentingYML = config;
		rentingManager = new Renters(config);
		timerStart();
	}

	public void saveConfig() {
		try {
			this.rentingManager.config.save(this.rentingYML);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void timerStart() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				FileConfiguration tmp = rentingManager.config;
				for (String regionName : tmp.getKeys(false)) {
					OfflinePlayer player = rentingManager.isRentingBy(regionName);
					if (player != null) {
						String payPeriod = rentingManager.rentPeriod(regionName, false);
						Integer interval = Integer.valueOf(payPeriod.split(";")[0]);
						String period = rentingManager.timePeriodFromAbbrevation(payPeriod.split(";")[1]);
						Date started = rentingManager.isRentedStarted(regionName);
						Double price = rentingManager.rentPrice(regionName);
						Double balance = Plugins.economy.getBalance(player);
						DefaultDomain owners = new DefaultDomain();
						
						World world = Bukkit.getServer().getWorld(rentingManager.rentWorld(regionName));
						RegionManager manager = worldGuard.getRegionManager(world);
						ProtectedRegion region = manager.getRegion(regionName);
						if (region == null) continue;
						
						owners = region.getOwners();
						if (owners == null) owners = new DefaultDomain();
		
						price /= owners.size();
						
						Double diff = ((new Date()).getTime() - started.getTime()) / (60.0 * 1000.0) % 60.0;
						if (period.equalsIgnoreCase("Invalid timespan"))
							continue;
						else if (period.equalsIgnoreCase("Hour")) 
							diff /= 60.0;
						else if (period.equalsIgnoreCase("Day")) 
							diff /= (diff / 60.0);
						else if (period.equalsIgnoreCase("Week")) 
							diff /= (diff / 60.0) / 7.0;
						else if (period.equalsIgnoreCase("Year")) 
							diff /= (diff / 60.0) / 365.0;
						while (diff > interval) {
							if (price > balance) {
								rentingManager.setIsNoLongerRentedBy(regionName);
								
								DefaultDomain members = region.getMembers();
								members.removePlayer(player.getName());
								
								try {
									region.setMembers(members);
									manager.save();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								if (player.isOnline())
									Bukkit.getPlayer(player.getUniqueId()).sendMessage(ChatColor.RED + "Good job, you were kicked out of " + regionName + " because you couldn't afford the rent.");
								else
								{
									if (Plugins.getEssentials() != null)
									{
										Plugins.getEssentials().getUser(player).addMail(ChatColor.RED + "Good job, you were kicked out of " + regionName + " because you couldn't afford the rent.");
									}
								}
								
								break;
							}
							else
							{
								for (String owner : owners.getPlayers()) {
									Plugins.economy.withdrawPlayer(player, price);
									Plugins.economy.depositPlayer(owner, price);
								}
								
								if (player.isOnline())
									Bukkit.getPlayer(player.getUniqueId()).sendMessage(ChatColor.GREEN + "Awe, look at you paying the rent for " + regionName + ".");
								else
								{
									if (Plugins.getEssentials() != null)
									{
										Plugins.getEssentials().getUser(player).addMail(ChatColor.GREEN + "Awe, look at you paying the rent for " + regionName + ".");
									}
								}

								rentingManager.setIsRentedStarted(regionName, new Date());
								saveConfig();
							}
							
							diff -= interval;
						}
					}
				}
			}
		}, 0, 60 * 1000);
	}
	
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = (Player) sender;
		if (args.length == 0) player.sendMessage(helpPage(1));
		else
		{
			String first = args[0];
			if (first.equalsIgnoreCase("help")) {
				onHelp(player, command, label, args);
			} 
			
			else if (first.equalsIgnoreCase("info")) {
				rentingManager.onInfo(player, command, label, args);
			}
			
			else if (first.equalsIgnoreCase("on")) {
				rentingManager.onRentOn(player, command, label, args);
			}
			
			else if (first.equalsIgnoreCase("off")) {
				rentingManager.onRentOff(player, command, label, args);
			}
			
			else if (first.equalsIgnoreCase("region")) {
				rentingManager.onRentRegion(player, command, label, args);
			}
			
			else if (first.equalsIgnoreCase("leave")) {
				rentingManager.onRentGiveUp(player, command, label, args);
			}
			
			else 
				onHelp(player, command, label, args);
		}
		
		return true;
	}
		
	private void onHelp(Player player, Command command, String label, String[] args) {
		Integer pageNumber = (args.length == 1) ? 1 : numberFromString(args[1]);
		player.sendMessage(helpPage(pageNumber));
	}
	
	
	private String helpPage(Integer page)
	{
		String header = "§b~§d~§f~§b §dD§fe§bs§dt§fi§bn§dy§f'§bs§d §fR§be§dn§ft§bi§dn§fg§b §dH§fe§bl§dp§f §b" + page + "§d/§f1§b §d~§f~§b~";
		switch (page)
		{
		case 1:
			return header
					+ ChatColor.GREEN + "To get info on a region for rent: /rent info (region name)\n"
					+ "To put your region for rent: /rent on (region name) [price] [timeframe]\n"
					+ "To remove your region from being rented: /rent off (region name)\n"
					+ "To rent a region: /rent region (region name)\n"
					+ "To leave a region you're renting: /rent leave (region name)\n"
					+ "To kick someone out of your region: /rent kick (region name) (reason)";
//		case 2:
//			return header
//			+ ChatColor.GREEN + "To allow others in your region: /ll friend (region name) (their name)\n"
//					+ "To remove others: /ll unfriend (region name) (their name)";
		}
		
		return "Invalid page.";
	}
	
	
	private Integer numberFromString(String string) {
		try
		{
			return Integer.parseInt(string);
		}
		catch (Exception ex)
		{
			return 1;
		}
	}
}
