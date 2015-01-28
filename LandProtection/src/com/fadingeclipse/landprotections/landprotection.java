package com.fadingeclipse.landprotections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.fadingeclipse.landprotections.Config.*;
import com.fadingeclipse.landprotections.Private.Regions;
import com.fadingeclipse.landprotections.Private.Visual;
import com.fadingeclipse.landprotections.Public.Plugins;
import com.fadingeclipse.landprotections.Rent.Rent;

public class landprotection extends JavaPlugin {
	public static landprotection instance;
	public static Rent rentManager;
	Regions regionManager;
	Visual visualManager;

	@Override
	public void onEnable() {
		Plugins.setupEconomy();
		Config.instance = new Config(this.getConfig());
		this.saveDefaultConfig();

		// setupRent();
		regionManager = new Regions();
		visualManager = new Visual();
		instance = this;
	}

	private void setupRent() {
		File rentYML = new File(this.getDataFolder(), "renting.yml");
		if (!rentYML.exists())
			try {
				rentYML.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		rentManager = new Rent(rentYML);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, final String[] args) {
		if (!(sender instanceof Player))
			return false;

		final Player player = (Player) sender;
		if (label.equalsIgnoreCase("ll")
				|| label.equalsIgnoreCase("landprotection")) {
			if (args.length == 0)
				player.sendMessage(helpPage(1));
			else {
				String first = args[0];

				/* Player wants to buy a region with name of args[1] */
				if (first.equalsIgnoreCase("buy")) {
					regionManager.onBuy(player, command, label, args);
				}

				/* Find Destiny's House */
				else if (first.equalsIgnoreCase("find")) {
					Executors.newSingleThreadExecutor().submit(new Runnable() {
						
						@Override
						public void run() {
							player.sendMessage("Started searching.");
							World world = Bukkit.getServer().getWorlds().get(0);
							String search = (args.length > 1) ? args[1] : "des";
							
							Integer x = 3000;
							Integer z = 3000;
							Integer y = 200;
							while (x > -2000)
							{
								while (y > 0)
								{						
									while (z > -2000)
									{
										player.sendMessage("No block at: " + x + " " + y + " " + z);
										z--;
									}
									
									y--;
								}
								
								x--;
							}
							x = 2000;
							z = 2000;
							y = 200;
							
							while (x > -2000)
							{
								while (y > 0)
								{						
									while (z > -2000)
									{
										Block b = player.getLocation().getWorld().getBlockAt(new Location(world, x, y, z));
										if (b instanceof Sign)
										{
											Sign sign = (Sign) b;
											for (String str : sign.getLines())
											{
												if (str.toLowerCase().contains(search)) 
												{
													player.sendMessage(sign.getLocation().toString());
													Bukkit.getServer().getLogger().info(sign.getLocation().toString());
												}
											}
										}
										else
										{
											player.sendMessage("No block at: " + x + " " + y + " " + z);
											Bukkit.getServer().getLogger().info("No block at: " + x + " " + y + " " + z);
										}
										
										z--;
									}
									
									y--;
								}
								
								x--;
							}
							
							player.sendMessage("Done");
						}
					});
				}

				/* Player wants to claim their region */
				else if (first.equalsIgnoreCase("claim")) {
					regionManager.onClaim(player, command, label, args);
				}

				/* Player wants to sell their region to a player or the server */
				else if (first.equalsIgnoreCase("sell")) {
					regionManager.onSell(player, command, label, args);
				}

				/*
				 * Player wants to rent a region with name of args[1] or put
				 * theirs up for rent
				 */
				else if (first.equalsIgnoreCase("rent")) {

				}

				/* Player like the person(s) they are adding from their region */
				else if (first.equalsIgnoreCase("trust")
						|| first.equalsIgnoreCase("friend")) {
					regionManager.onTrust(player, command, label, args);
				}

				/*
				 * Player doesn't like the person(s) they are removing from
				 * their region
				 */
				else if (first.equalsIgnoreCase("untrust")
						|| first.equalsIgnoreCase("unfriend")
						|| first.equalsIgnoreCase("gtfo")) {
					regionManager.onUnTrust(player, command, label, args);
				}

				/* Player would like to do something with their selection */
				// else if (first.equalsIgnoreCase("sel") ||
				// first.equalsIgnoreCase("selection")) {
				// visualManager.onSelection(player, command, label, args);
				// }

				/*
				 * Player would like to see how much a selection costs to claim
				 * or how much a region costs to surround
				 */
				else if (first.equalsIgnoreCase("cost")) {
					if (args.length < 2) {
						ArrayList<String> argsList = new ArrayList<String>();
						argsList.addAll(Arrays.asList(args));
						argsList.add(0, "ll");

						String[] array = new String[argsList.size()];
						for (int j = 0; j < argsList.size(); j++) {
							array[j] = argsList.get(j);
						}

						visualManager
								.onSelection(player, command, label, array);
					}
				}

				/* Player would like to surround their region */
				// else if (first.equalsIgnoreCase("surround")) {
				// regionManager.onSurround(player, command, label, args);
				// }

				/* Player has no idea what to do */
				else if (first.equalsIgnoreCase("help")) {
					onHelp(player, command, label, args);
				}

				else if (first.equalsIgnoreCase("reload")
						&& player.hasPermission("landprotection.admin.reload")) {
					this.reloadConfig();

					Config.instance.config = this.getConfig();
					Config.instance.Reload();

					player.sendMessage(ChatColor.GREEN + "Reload complete.");
				}

				else if (first.equalsIgnoreCase("pbb")
						&& player.hasPermission("landprotection.admin.reload")) {
					player.sendMessage("Price per block is: "
							+ Config.instance.pricePB);
				}
			}
		} else if (label.equalsIgnoreCase("rent")) {
			// return rentManager.onCommand(sender, command, label, args);
		}

		return true;
	}

	@Override
	public void onDisable() {
		// rentManager.saveConfig();
	}

	public void onHelp(Player player, Command command, String label,
			String[] args) {
		Integer pageNumber = (args.length == 1) ? 1 : numberFromString(args[1]);
		player.sendMessage(helpPage(pageNumber));
	}

	private String helpPage(Integer page) {
		String header = "§b~§d~§f~§b §dD§fe§bs§dt§fi§bn§dy§f'§bs§d §fL§ba§dn§fd§bP§dr§fo§bt§de§fc§bt§di§fo§bn§d §fH§be§dl§fp§b §d"
				+ page + "§f/§b2§d §f~§b~§d~\n";
		switch (page) {
		case 1:
			return header
					+ ChatColor.GREEN
					+ "To claim your own region: /ll claim\n"
					+ "To see how much a claim costs: /ll cost\n"
					+ "To sell your region to the server: /ll sell (region name)\n"
					+ "To put your region up for sale: /ll sell (region name) (price)\n"
					+ "To buy someone else's region: /ll buy (region name)\n";
		case 2:
			return header
					+ ChatColor.GREEN
					+ "To allow others in your region: /ll friend (region name) (their name)\n"
					+ "To remove others: /ll unfriend (region name) (their name)";
		}

		return "Invalid page.";
	}

	private Integer numberFromString(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception ex) {
			return 1;
		}
	}
}
