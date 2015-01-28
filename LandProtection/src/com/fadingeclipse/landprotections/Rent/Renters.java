package com.fadingeclipse.landprotections.Rent;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.fadingeclipse.landprotections.landprotection;
import com.fadingeclipse.landprotections.Public.Plugins;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Renters {
	public FileConfiguration config;
	private WorldGuardPlugin worldGuard = Plugins.getWorldGuard();

	public Renters(File config) {
		this.config = YamlConfiguration.loadConfiguration(config);
	}

	public void onInfo(Player player, Command command, String label,
			String[] args) {
		if (args.length < 2)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /sell info (region name)\n"
					+ "Example: /sell info town_house1");
		else {
			String regionName = args[1];
			RegionManager regionManager = worldGuard.getRegionManager(player
					.getWorld());
			ProtectedRegion region = regionManager.getRegion(regionName);
			if (region == null)
				player.sendMessage(ChatColor.RED
						+ "A region with that name does not exist.");
			else {
				NumberFormat formatter = NumberFormat.getCurrencyInstance();
				String moneyString = formatter.format(rentPrice(regionName));
				String rentPeriod = rentPeriod(regionName, true);

				player.sendMessage(ChatColor.GREEN + "The region " + regionName
						+ " " + ((isRentable(regionName)) ? "is" : "not")
						+ " for rent.\n" + "Rent Price: " + moneyString + ".\n"
						+ "Rent Period: " + rentPeriod + ".");
			}
		}
	}

	public void onRentOn(Player player, Command command, String label,
			String[] args) {
		if (args.length < 2)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /rent on (region name)");
		else {
			String regionName = args[1];
			if (args.length < 3)
				setIsRentable(regionName, true);
			else {
				Double price = 0.0;
				RegionManager regionManager = worldGuard
						.getRegionManager(player.getWorld());
				ProtectedRegion region = regionManager.getRegion(regionName);
				if (region == null)
					player.sendMessage(ChatColor.RED
							+ "A region with that name does not exist.");
				else if (!region.getOwners().contains(
						worldGuard.wrapPlayer(player)))
					player.sendMessage(ChatColor.RED
							+ "You do not have permission to put this region for rent.");
				else if (isRentingBy(regionName) != null)
					player.sendMessage(ChatColor.RED
							+ "Someone is already renting this region.\n"
							+ "If you would like to kick them out, use /rent kick (region name) (reason)");
				else {
					try {
						price = Double.valueOf(args[2]);
					} catch (Exception ex) {
						player.sendMessage(ChatColor.RED + "Incorrect format.");
						return;
					}

					String rentPeriod = rentPeriod(regionName, false);
					if (args.length > 3) {
						String third = args[3];
						if (third.matches("^[0-9]+;[m,s,d,h,w]$")) {
							rentPeriod = third;
						} else {
							player.sendMessage(ChatColor.RED
									+ "Incorrect format.");
							return;
						}
					}

					setIsRentable(regionName, true);
					setRentPrice(regionName, price);
					setRentPeriod(regionName,
							Integer.valueOf(rentPeriod.split(";")[0]),
							rentPeriod.split(";")[1]);
					setRentWorld(regionName, player.getWorld().getName());
				}
			}

			player.sendMessage(ChatColor.GREEN + "Your region is now for rent.");
			landprotection.rentManager.saveConfig();
		}
	}

	public void onRentOff(Player player, Command command, String label,
			String[] args) {
		if (args.length < 2)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /rent off (region name)");
		else {
			String regionName = args[1];
			RegionManager regionManager = worldGuard.getRegionManager(player
					.getWorld());
			ProtectedRegion region = regionManager.getRegion(regionName);
			if (region == null)
				player.sendMessage(ChatColor.RED
						+ "A region with that name does not exist.");
			else if (!region.getOwners()
					.contains(worldGuard.wrapPlayer(player)))
				player.sendMessage(ChatColor.RED
						+ "You do not have permission to put this house for rent.");
			else {
				player.sendMessage(ChatColor.GREEN
						+ "Your region is no longer for rent.");

				setIsRentable(regionName, false);
				landprotection.rentManager.saveConfig();
			}
		}
	}

	public void onRentKick(Player player, Command command, String label,
			String[] args) {
		if (args.length < 3)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /rent kick (region name) (reason)");
		else
		{
			String regionName = args[1];
			RegionManager regionManager = worldGuard
					.getRegionManager(player.getWorld());
			ProtectedRegion region = regionManager.getRegion(regionName);
			if (region == null)
				player.sendMessage(ChatColor.RED
						+ "A region with that name does not exist.");
			else if (!region.getOwners().contains(
					worldGuard.wrapPlayer(player)))
				player.sendMessage(ChatColor.RED
						+ "You do not have permission to put kick someone from this region.");
			else
			{
				setIsRentable(regionName, true);
				setIsNoLongerRentedBy(regionName);
				
				landprotection.rentManager.saveConfig();
			}
		}
	}
	
	public void onRentRegion(Player player, Command command, String label,
			String[] args) {
		if (args.length < 2)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /sell region (region name)");
		else {
			String regionName = args[1];
			RegionManager regionManager = worldGuard.getRegionManager(player
					.getWorld());
			ProtectedRegion region = regionManager.getRegion(regionName);
			if (region == null)
				player.sendMessage(ChatColor.RED
						+ "A region with that name does not exist.");
			else if (region.getOwners().contains(worldGuard.wrapPlayer(player)))
				player.sendMessage(ChatColor.RED
						+ "You cannot rent a region you own.\n"
						+ "That's not how this works.\n"
						+ "That's now how any of this works.\n"
						+ "We unfriend you.");
			else if (!isRentable(regionName)) {
				player.sendMessage(ChatColor.RED
						+ "That region is not for rent.");
			} else {
				Double price = rentPrice(regionName);
				if (Plugins.economy.getBalance(player.getPlayer()) >= price) {
					DefaultDomain members = region.getMembers();
					members.addPlayer(worldGuard.wrapPlayer(player));
					
					try {
						region.setMembers(members);
						regionManager.save();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					setIsRentingBy(regionName, player);
					setIsRentable(regionName, false);

					landprotection.rentManager.saveConfig();
					Plugins.economy.withdrawPlayer(player.getPlayer(), price);

					player.sendMessage(ChatColor.GREEN
							+ "You are now renting this region.");
				} else
					player.sendMessage(ChatColor.RED
							+ "You cannot afford to rent this region.");
			}
		}
	}

	public void onRentGiveUp(Player player, Command command, String label,
			String[] args) {
		if (args.length < 2)
			player.sendMessage(ChatColor.RED
					+ "Correct format: /rent leave (region name)");
		else {
			String regionName = args[1];
			RegionManager regionManager = worldGuard.getRegionManager(player
					.getWorld());
			ProtectedRegion region = regionManager.getRegion(regionName);
			OfflinePlayer renter = isRentingBy(regionName);
			
			if (region == null)
				player.sendMessage(ChatColor.RED
						+ "A region with that name does not exist.");
			else if (renter == null)
				player.sendMessage(ChatColor.RED + "What? No one is even renting this place.");
			else if (!renter.equals(player))
				player.sendMessage(ChatColor.RED
						+ "You cannot give up a region you're not renting.\n"
						+ "How would you like it if someone did that to you?");
			else {
				setIsNoLongerRentedBy(regionName);
				landprotection.rentManager.saveConfig();

				player.sendMessage(ChatColor.GREEN
						+ "Welp, you no longer own this region. Oh well.");
			}
		}
	}

	public void setRentWorld(String regionName, String world) {
		this.config.set(regionName + ".world", world);
	}
	
	public String rentWorld(String regionName) {
		return this.config.getString(regionName + ".world");
	}
	
	public boolean isRentable(String regionName) {
		return this.config.getBoolean(regionName + ".rentable", false);
	}
	
	public void setIsRentable(String regionName, boolean rentable) {
		config.set(regionName + ".rentable", rentable);
	}

	public Double rentPrice(String regionName) {
		return this.config.getDouble(regionName + ".price", 250.0);
	}

	public void setRentPrice(String regionName, Double rentPrice) {
		config.set(regionName + ".price", rentPrice);
	}

	public String rentPeriod(String regionName, boolean formatted) {
		String period = this.config
				.getString(regionName + ".rentPeriod", "1;d");
		String span = timePeriodFromAbbrevation(period.split(";")[1]);
		Integer time = Integer.valueOf(period.split(";")[0]);
		if (time > 1)
			span += "s";

		return (formatted) ? "Once per every " + time + " "
				+ span.toLowerCase() : period;
	}

	public void setRentPeriod(String regionName, Integer timespan, String period) {
		config.set(regionName + ".rentPeriod", timespan + ";" + period);
	}

	public Date isRentedStarted(String regionName) {
		String start = config.getString(regionName + ".renter.date");
		if (start == null) return null;
		
		try {
			return new SimpleDateFormat("dd-MM-yy:HH:mm:SS").parse(start);
			
			//Wed Nov 19 19:05:40 EST 2014
		} catch (ParseException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public void setIsRentedStarted(String regionName, Date date) {
		config.set(regionName + ".renter.date", new SimpleDateFormat("dd-MM-yy:HH:mm:SS").format(date));
	}
	
	public OfflinePlayer isRentingBy(String regionName) {
		String uuid = config.getString(regionName + ".renter.uuid");
		if (uuid == null)
			return null;

		UUID playerId = UUID.fromString(uuid);
		return Bukkit.getOfflinePlayer(playerId);
	}

	public void setIsRentingBy(String regionName, Player player) {
		config.set(regionName + ".renter.date", new SimpleDateFormat("dd-MM-yy:HH:mm:SS").format(new Date()));
		config.set(regionName + ".renter.name", player.getName());
		config.set(regionName + ".renter.uuid", player.getUniqueId().toString());
	}

	public void setIsNoLongerRentedBy(String regionName) {
		config.set(regionName + ".renter", null);
	}

	public String timePeriodFromAbbrevation(String abbr) {
		if (abbr.equalsIgnoreCase("m")) {
			return "Minute";
		} else if (abbr.equalsIgnoreCase("h")) {
			return "Hour";
		} else if (abbr.equalsIgnoreCase("d")) {
			return "Day";
		} else if (abbr.equalsIgnoreCase("w")) {
			return "Week";
		} else if (abbr.equalsIgnoreCase("y")) {
			return "Year";
		} else {
			return "Invalid timespan";
		}
	}
}
