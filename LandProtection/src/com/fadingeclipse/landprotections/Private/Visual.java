package com.fadingeclipse.landprotections.Private;

import java.text.NumberFormat;

import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.fadingeclipse.landprotections.Config.*;

import com.fadingeclipse.landprotections.Public.Plugins;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Visual {
	WorldGuardPlugin worldGuard = Plugins.getWorldGuard();
	
	public void onSelection(Player player, Command command, String label, String[] args) {
		if (args.length < 2)
		{
			onHelp(player);
		}
		else
		{
			Selection selection = Plugins.getWorldEdit().getSelection(player);
			if (selection == null)
			{
				player.sendMessage(ChatColor.RED + "You need to make a selection with //wand first.");
			}
			else
			{
				String second = args[1];
				if (second.equalsIgnoreCase("cost")) {
					onCost(player, command, label, args, selection);
				}
				else if (second.equalsIgnoreCase("display")) {
					onDisplay(player, command, label, args, selection);
				}
				else if (second.equalsIgnoreCase("expand")) {
					onExpand(player, command, label, args, selection);
				}
				else
				{
					onHelp(player);
				}
			}
		}
	}
	
	public void onHelp(Player player) {
		player.sendMessage(ChatColor.GREEN + "Cost to claim: /ll sel cost\n"
				+ "Clear your selection: /ll sel clear\n"
				+ "Show your selection visually: /ll sel display \n"
				+ "Expand your selection: /ll sel expand [up, down] #\n");
	}
	
	public void onCost(Player player, Command command, String label, String[] args, Selection selection) {
		if (args.length == 2) {
			Double price = (selection.getArea() * Config.instance.pricePB);
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			String moneyString = formatter.format(price);
			
			player.sendMessage(ChatColor.GREEN + "This selection would cost " + moneyString + " to claim.");
		}
	}
	
	public void onDisplay(Player player, Command command, String label, String[] args, Selection selection) {
		
	}
	
	public void onExpand(Player player, Command command, String label, String[] args, Selection selection) {
		
	}
}
