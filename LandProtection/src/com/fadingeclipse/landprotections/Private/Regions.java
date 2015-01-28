package com.fadingeclipse.landprotections.Private;

import java.text.NumberFormat;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fadingeclipse.landprotections.Public.*;
import com.fadingeclipse.landprotections.Config.*;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.ChatColor;

public class Regions {
	WorldGuardPlugin worldGuard = Plugins.getWorldGuard();
	
	public void onClaim(Player player, Command command, String label, String[] args) {
		if (args.length < 2)
		{
			player.sendMessage(ChatColor.RED + "You need to say what you'd like to name your region.");
			return;
		}
		
		Selection selection = Plugins.getWorldEdit().getSelection(player);
		if (selection == null)
		{
			player.sendMessage(ChatColor.RED + "You need to make a selection with //wand first.");
			return;
		}
		
		String rgName = args[1];
		RegionManager manager = worldGuard.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(rgName);
		if (region != null)
		{
			player.sendMessage(ChatColor.RED + "A region with that name already exist.");
			return;
		}
		
		region = new ProtectedCuboidRegion(rgName, selection.getNativeMinimumPoint().toBlockVector(), selection.getNativeMaximumPoint().toBlockVector());
		ApplicableRegionSet r1 = manager.getApplicableRegions(region);
		
		DefaultDomain owners = new DefaultDomain();
		owners.addPlayer(worldGuard.wrapPlayer(player));
		region.setOwners(owners);
		
		String nonOwn = new String();
		for (ProtectedRegion r : r1) {
			if (!r.getOwners().contains(player.getName())) {
				nonOwn = r.getId() + "\n";
			}
		}
		
		if (r1.size() == 0 || r1.isOwnerOfAll(worldGuard.wrapPlayer(player)))
		{
			Double price = (selection.getArea() * Config.instance.pricePB);
			int priority = 1;
			
			Boolean isInside = false;
			for (ProtectedRegion r : r1) {
				if (!isInside) isInside = r.containsAny(region.getPoints());
				if (isInside && priority < r.getPriority()) priority += (r.getPriority() + 1);
			}
			
			if (isInside)
			{
				price = 0.0;
				region.setPriority(priority);
			}
			
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			String moneyString = formatter.format(price);
			
			if (Plugins.economy.getBalance(player.getPlayer()) < price)	
				player.sendMessage(ChatColor.RED + "You cannot afford to buy this region.\nThis region costs " + moneyString + ".");
			else
			{
//				player.sendMessage(ChatColor.YELLOW + "Please wait while we claim this region for you...");
				Plugins.economy.withdrawPlayer(player.getPlayer(), price);
				manager.addRegion(region);

//				int xMin = selection.getMinimumPoint().getBlockX();
//				int yMin = selection.getMinimumPoint().getBlockY();
//				int zMin = selection.getMinimumPoint().getBlockZ();
//
//				int xMax = selection.getMaximumPoint().getBlockX();
//				int yMax = selection.getMaximumPoint().getBlockY();
//				int zMax = selection.getMaximumPoint().getBlockZ();
//				//same for loc2 with xMax,yMax and zMax...
//				 
//				ArrayList<Block> blocks = new ArrayList<Block>();
//				World w = selection.getMinimumPoint().getWorld();
//				 
//				for (int i = xMin; i<xMax; i++)
//				{
//				  for (int j = yMin; j<yMax; j++)
//				  {
//				      for (int k = zMin; j<zMax; k++)
//				      {
//				          blocks.add(w.getBlockAt(i,j,k));
//				      }
//				  }
//				}
//				
//				YamlConfiguration config = Config.instance.getConfigForRegion(rgName);
//				for (Block block : blocks) {
//					String cords = block.getY() + "." + block.getX() + "." + block.getZ();
//					config.set(cords + ".block", block.);
//				}
				
				try {
					manager.save();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (price > 0.0) player.sendMessage(ChatColor.GREEN + "You bought the region for " + moneyString + ".");
				else player.sendMessage(ChatColor.GREEN + "This region is now yours.");
				
				try {
					region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(worldGuard, player, "Welcome to " + player.getDisplayName() + "'s region!"));
				} catch (InvalidFlagFormat e) {
					e.printStackTrace();
				}
			}
		}
		else
		{
			nonOwn = nonOwn.substring(0, nonOwn.length() - "\n".length());
			player.sendMessage(ChatColor.RED + "Your region is intersecting region(s):\n" + nonOwn + "");
		}
	}
	
	public void onTrust(Player player, Command command, String label, String[] args) {
		if (args.length < 3)
		{
			player.sendMessage(ChatColor.RED + "You need to say what you'd like to name your region and who you'd like to add to the region.");
			return;
		}
		
		String rgName = args[1];
		RegionManager manager = worldGuard.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(rgName);
		if (region == null)
		{
			player.sendMessage(ChatColor.RED + "A region with that name doesn't exist.");
		}
		else if (!region.isOwner(worldGuard.wrapPlayer(player)))
		{
			player.sendMessage(ChatColor.RED + "You cannot trust players in a region that you are not the owner of.");
		}
		else
		{
			DefaultDomain members = region.getMembers();
			for (Integer i = 2; i < args.length; i ++) {
				members.addPlayer(args[i]);
			}
			
			region.setMembers(members);
			
			try
			{
				manager.save();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public void onUnTrust(Player player, Command command, String label, String[] args) {
		if (args.length < 3)
		{
			player.sendMessage(ChatColor.RED + "You need to say what you'd like to name your region and who you'd like to remove from the region.");
			return;
		}
		
		String rgName = args[1];
		RegionManager manager = worldGuard.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(rgName);
		if (region == null)
		{
			player.sendMessage(ChatColor.RED + "A region with that name doesn't exist.");
		}
		else if (!region.isOwner(worldGuard.wrapPlayer(player)))
		{
			player.sendMessage(ChatColor.RED + "You cannot untrust players in a region that you are not the owner of.");
		}
		else
		{
			DefaultDomain members = region.getMembers();
			for (Integer i = 2; i < args.length; i ++) {
				members.removePlayer(args[i]);
			}
			
			region.setMembers(members);
			
			try
			{
				manager.save();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
//	public void onSurround(Player player, Command command, String label, String[] args) {
//		if (args.length < 2)
//		{
//			player.sendMessage(ChatColor.RED + "You need to say what region you'd like to surround with a glowstone pattern.");
//		}
//		else
//		{
//			String rgName = args[1];
//			RegionManager manager = worldGuard.getRegionManager(player.getWorld());
//			ProtectedRegion region = manager.getRegion(rgName);
//			if (region == null)
//			{
//				player.sendMessage(ChatColor.RED + "A region with that name doesn't exist.");
//			}
//			else if (!region.isOwner(worldGuard.wrapPlayer(player)) && !region.isMember(worldGuard.wrapPlayer(player)))
//			{
//				player.sendMessage(ChatColor.RED + "You cannot surround a region that you're not the owner or a member of.");
//			}
//			else
//			{
//				if (args.length < 3) /* Surround it */
//				{
//					Double price = (region.volume() * Config.instance.pricePB) * 0.25;
//					if (Plugins.economy.getBalance(player.getPlayer()) < price)
//						player.sendMessage(ChatColor.RED + "You cannot afford to surround this region.\n"
//								+ "This selection would cost " + NumberFormat.getCurrencyInstance().format(price) + " to surround with glowstone.");
//					else
//					{
//						BlockVector2D v1 = region.getPoints().get(0);
//						for (int i = 1; i < region.getPoints().size(); i++) {
//							BlockVector2D v2 = region.getPoints().get(i);
//							
//							
//						}
//					}
//				}
//				else /* Display what the cost is to surround it */
//				{
//					Double price = (region.volume() * Config.instance.pricePB);
//					NumberFormat formatter = NumberFormat.getCurrencyInstance();
//					String moneyString = formatter.format(price * 0.25);
//					
//					player.sendMessage(ChatColor.GREEN + "This selection would cost " + moneyString + " to surround with glowstone.");
//				}
//			}
//		}
//	}
	
	public void onBuy(Player player, Command command, String label, String[] args)
	{
		if (args.length < 2)
		{
			player.sendMessage(ChatColor.RED + "You need to say what region you'd like to buy.");
			return;
		}
		
		String rgName = args[1];
		RegionManager manager = worldGuard.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(rgName);
		if (region == null)
		{
			player.sendMessage(ChatColor.RED + "A region with that name doesn't exist.");
			return;
		}
		else if (region.getOwners().contains(player.getUniqueId()))
		{
			player.sendMessage(ChatColor.RED + "You cannot buy a region you own. If you'd like to remove it from being sold, run /ll sell remove");
			return;
		}
		else if (region.getFlag(DefaultFlag.PRICE) == null) {
			player.sendMessage(ChatColor.RED + "That region is not buyable.");
			return;
		}
		
		Double price = region.getFlag(DefaultFlag.PRICE).doubleValue();
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		String moneyString = formatter.format(price);
		
		if (Plugins.economy.getBalance(player.getPlayer()) < price) {
			player.sendMessage(ChatColor.RED + "You do not have enough money to buy this region.\nThis region costs " + moneyString);
			return;
		} 
		else
		{
			Plugins.economy.withdrawPlayer(player.getPlayer(), price);
			
			DefaultDomain oldOwners = region.getOwners();
			Double total = price / oldOwners.size();
			
			for (String owner : oldOwners.getPlayers())
			{
				Player ownerPl = Bukkit.getPlayerExact(owner);
				if (ownerPl != null)  {
					if (ownerPl.isOnline()) ownerPl.sendMessage("Your region " + rgName + " was bought by " + player.getDisplayName() + " for " + moneyString);
				}
				else
				{
					if (Plugins.getEssentials() != null)
					{
						Plugins.getEssentials().getUser(owner).addMail("Your region " + rgName + " was bought by " + player.getDisplayName() + " for " + moneyString);
					}
				}
				
				if (ownerPl != null) Plugins.economy.depositPlayer(ownerPl, total);
				else Plugins.economy.depositPlayer(Bukkit.getOfflinePlayer(owner), total);
			}
			
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(worldGuard.wrapPlayer(player));
			region.setOwners(owners);

			region.setFlag(DefaultFlag.PRICE, null);
			region.setFlag(DefaultFlag.BUYABLE, null);
			
			player.sendMessage(ChatColor.GREEN + "You successfully bought " + rgName + " for " + moneyString + ".");
			try {
				region.setFlag(DefaultFlag.GREET_MESSAGE, DefaultFlag.GREET_MESSAGE.parseInput(worldGuard, player, "Welcome to " + player.getDisplayName() + "'s region!"));
			} catch (InvalidFlagFormat e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				manager.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onSell(Player player, Command command, String label, String[] args) {
		/* Player sells region to server */
		
		if (args.length < 2)
		{
			player.sendMessage(ChatColor.RED + "You need to say what region you'd like to sell.");
			return;
		}
		
		/* /ll sell <region name> [price] */
		
		String rgName = args[1];
		RegionManager manager = worldGuard.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(rgName);
		if (region == null)
		{
			player.sendMessage(ChatColor.RED + "A region with that name doesn't exist.");
			return;
		}
		else if (!region.getOwners().contains(player.getUniqueId()))
		{
			player.sendMessage(ChatColor.RED + "You do not own the region named " + rgName + ".");
			return;
		}
		
		if (args.length == 2)
		{
			Double price = (region.volume() * Config.instance.pricePB) * 0.75f;
			
			Plugins.economy.depositPlayer(player.getPlayer(), price);
			manager.removeRegion(rgName);
			
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			String moneyString = formatter.format(price);
			
			player.sendMessage(ChatColor.GREEN + "Your region was sold to the server for " + moneyString);
			
			try {
				manager.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		/* Player puts region up for sale for price */
		else if (args.length == 3)
		{
			String third = args[2];
			if (third.equalsIgnoreCase("remove")) {
				DoubleFlag priceFlag = DefaultFlag.PRICE;
				region.setFlag(priceFlag, null);
				
				StringFlag greetingFlag = DefaultFlag.GREET_MESSAGE;
				region.setFlag(greetingFlag, null);
				region.setFlag(DefaultFlag.BUYABLE, null);
				
				try {
					manager.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (third.equalsIgnoreCase("gen") || third.equalsIgnoreCase("generate") || third.equalsIgnoreCase("auto")) {
				try
				{
					Double price = (region.volume() * Config.instance.pricePB);					
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = formatter.format(price);
					
					DoubleFlag priceFlag = DefaultFlag.PRICE;
					region.setFlag(priceFlag, priceFlag.parseInput(worldGuard, player, price.toString()));
					
					StringFlag greetingFlag = DefaultFlag.GREET_MESSAGE;
					region.setFlag(greetingFlag, greetingFlag.parseInput(worldGuard, player, "This region is for sale by " + player.getDisplayName() + " for " + moneyString + ".\n" + "Run /ll buy " + rgName + " to buy this region."));
					region.setFlag(DefaultFlag.BUYABLE, DefaultFlag.BUYABLE.parseInput(worldGuard, player, "true"));
					
					manager.save();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				try
				{
					Double price = Double.parseDouble(third);
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = formatter.format(price);
					
					DoubleFlag priceFlag = DefaultFlag.PRICE;
					region.setFlag(priceFlag, priceFlag.parseInput(worldGuard, player, price.toString()));
					
					StringFlag greetingFlag = DefaultFlag.GREET_MESSAGE;
					region.setFlag(greetingFlag, greetingFlag.parseInput(worldGuard, player, "This region is for sale by " + player.getDisplayName() + " for " + moneyString + ".\n" + "Run /ll buy " + rgName + " to buy this region."));
					region.setFlag(DefaultFlag.BUYABLE, DefaultFlag.BUYABLE.parseInput(worldGuard, player, "true"));
					
					manager.save();
				}
				catch (Exception ex)
				{
					if (ex instanceof NumberFormatException) player.sendMessage(ChatColor.RED + "The correct format is: /ll sell ##.## or /ll sell 50.00");
				}
			}
		}
	}
}
