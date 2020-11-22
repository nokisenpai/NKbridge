package be.noki_senpai.NKbridge.managers;

import be.noki_senpai.NKbridge.NKbridge;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Manager
{
	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;
	private DatabaseManager databaseManager = null;
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private GroupManager groupManager = null;
	private InventoryManager inventoryManager = null;

	public Manager(NKbridge instance)
	{
		console = Bukkit.getConsoleSender();
		configManager = new ConfigManager(instance.getConfig());
		databaseManager = new DatabaseManager(configManager);
		playerManager = new PlayerManager();
		queueManager = new QueueManager();
		groupManager = new GroupManager();
		inventoryManager = new InventoryManager(groupManager, playerManager);
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Console
	public ConsoleCommandSender getConsole()
	{
		return console;
	}

	// PluginManager
	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	// DatabaseManager
	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	// PlayerManager
	public PlayerManager getPlayerManager()
	{
		return playerManager;
	}

	// QueueManager
	public QueueManager getQueueManager()
	{
		return queueManager;
	}

	// GroupManager
	public GroupManager getGroupManager()
	{
		return groupManager;
	}

	// InventoryManager
	public InventoryManager getInventoryManager()
	{
		return inventoryManager;
	}
}
