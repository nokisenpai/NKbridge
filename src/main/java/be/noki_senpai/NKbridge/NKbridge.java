package be.noki_senpai.NKbridge;

import be.noki_senpai.NKbridge.cmd.BridgeCmd;
import be.noki_senpai.NKbridge.listeners.BridgeCompleter;
import be.noki_senpai.NKbridge.listeners.NKbridgeListeners;
import be.noki_senpai.NKbridge.listeners.PlayerConnectionListener;
import be.noki_senpai.NKbridge.managers.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NKbridge extends JavaPlugin
{
	public final static String PNAME = "[NKbridge]";
	private Manager manager = null;
	private ConsoleCommandSender console = null;
	private static NKbridge plugin = null;

	// Fired when plugin is first enabled
	@Override
	public void onEnable()
	{
		plugin = this;
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
		this.saveDefaultConfig();

		console = Bukkit.getConsoleSender();
		manager = new Manager(this);

		if(!checkNKmanager())
		{
			console.sendMessage(ChatColor.DARK_RED + PNAME + " NKmanager in not enabled !");
			disablePlugin();
			return;
		}

		// Load configuration
		if(!manager.getConfigManager().loadConfig())
		{
			disablePlugin();
			return;
		}

		// Load database connection (with check)
		if(!manager.getDatabaseManager().loadDatabase())
		{
			disablePlugin();
			return;
		}

		// Load database connection (with check)
		if(!manager.getGroupManager().loadData())
		{
			disablePlugin();
			return;
		}

		// /!\
		// Load players for online players
		manager.getPlayerManager().loadPlayer();
		manager.getInventoryManager().loadData();

		// Register listeners
		getServer().getPluginManager().registerEvents(new PlayerConnectionListener(manager.getPlayerManager(), manager.getInventoryManager(), manager.getQueueManager()), this);
		getServer().getPluginManager().registerEvents(new NKbridgeListeners(manager.getPlayerManager(), manager.getInventoryManager(), manager.getQueueManager(), manager.getGroupManager()), this);

		// Set tabulation completers
		getCommand("bridge").setTabCompleter(new BridgeCompleter(manager.getGroupManager()));

		// Register commands
		getCommand("bridge").setExecutor(new BridgeCmd(manager.getGroupManager(), manager.getQueueManager(), manager.getInventoryManager()));

		console.sendMessage(ChatColor.WHITE + "     .--. ");
		console.sendMessage(ChatColor.WHITE + "     |   '.   " + ChatColor.GREEN + PNAME + " by NoKi_senpai - successfully enabled !");
		console.sendMessage(ChatColor.WHITE + "'-..____.-'");
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable()
	{
		manager.getInventoryManager().saveInventories();
		manager.getPlayerManager().unloadPlayer();
		manager.getDatabaseManager().unloadDatabase();
		console.sendMessage(ChatColor.GREEN + PNAME + " has been disable.");
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter 'plugin'
	public static NKbridge getPlugin()
	{
		return plugin;
	}

	// ######################################
	// Disable this plugin
	// ######################################

	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}

	// ######################################
	// Check if NKmanager is enabled
	// ######################################

	public boolean checkNKmanager()
	{
		return getServer().getPluginManager().getPlugin("NKmanager").isEnabled();
	}
}
