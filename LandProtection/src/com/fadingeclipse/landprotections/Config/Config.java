package com.fadingeclipse.landprotections.Config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fadingeclipse.landprotections.landprotection;

public class Config {
	public Double pricePB;
	public static Config instance;
	public FileConfiguration config;
	
	public Config(FileConfiguration config) {
		this.config = config;
		this.pricePB = config.getDouble("price-per-block", 1.0);
	}
	
	public YamlConfiguration getConfigForRegion(String regionName) {
		File regionConfig = new File(landprotection.instance.getDataFolder() + File.pathSeparator + "Regions", regionName + ".yml");
		if (!regionConfig.exists())
			try {
				regionConfig.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return YamlConfiguration.loadConfiguration(regionConfig);
	}
	
	public void Reload() {
		this.pricePB = config.getDouble("price-per-block", 1.0);
	}
}
