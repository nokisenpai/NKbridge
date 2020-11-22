package be.noki_senpai.NKbridge.cmd.Bridge.Group;

import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.InventoryManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import be.noki_senpai.NKbridge.utils.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class AddToGroup
{
	GroupManager groupManager = null;
	QueueManager queueManager = null;
	InventoryManager inventoryManager = null;

	public AddToGroup(GroupManager groupManager, QueueManager queueManager, InventoryManager inventoryManager)
	{
		this.groupManager = groupManager;
		this.queueManager = queueManager;
		this.inventoryManager = inventoryManager;
	}

	public boolean addToGroup(CommandSender sender, String[] args)
	{
		// If no more argument
		if(args.length == 2)
		{
			sender.sendMessage(ChatColor.GREEN + "/bridge group addworld <groupName> [worldName]");
			return true;
		}

		// Command called by a player
		if(sender instanceof Player)
		{
			if(args.length >= 3)
			{
				if(!CheckType.isAlphaNumeric(args[2]))
				{
					sender.sendMessage(ChatColor.RED + "Nom de groupe non conforme.");
					return true;
				}

				String groupName = args[2].toLowerCase();
				if(!groupManager.existGroup(groupName))
				{
					sender.sendMessage(ChatColor.RED + "Le groupe '" + args[2] + "' n'existe pas.");
					return true;
				}

				String worldName = ((Player) sender).getWorld().getName();
				if(args.length >= 4)
				{
					if(Bukkit.getWorld(args[3]) == null)
					{
						sender.sendMessage(ChatColor.RED + "Le monde '" + args[3] + "' n'existe pas.");
						return true;
					}
					worldName = args[3];
				}

				if(groupManager.isWorldInGroup(groupName, worldName))
				{
					sender.sendMessage(ChatColor.RED + "Le monde '" + worldName + "' est déjà dans le groupe '" + args[2] + "'.");
					return true;
				}

				if(groupManager.getGroupId(worldName) != -1 )
				{
					sender.sendMessage(ChatColor.RED + "Le monde '" + worldName + "' est déjà dans un groupe.");
					return true;
				}

				String finalWorldName = worldName;
				queueManager.addToQueue(new Function()
				{
					@Override
					public Object apply(Object o)
					{
						inventoryManager.saveInventories(finalWorldName);
						groupManager.addToGroup(groupName, finalWorldName);
						inventoryManager.loadInventories(finalWorldName);
						sender.sendMessage(ChatColor.GREEN + "Le monde '" + finalWorldName + "' a été ajouté dans le groupe '" + args[2] + "'.");
						return null;
					}
				});
			}
		}
		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			if(args.length <= 3)
			{
				sender.sendMessage(ChatColor.RED + "/bridge group addworld <groupName> <worldName>");
				return true;
			}

			if(!CheckType.isAlphaNumeric(args[2]))
			{
				sender.sendMessage(ChatColor.RED + "Nom de groupe non conforme.");
				return true;
			}

			String groupName = args[2].toLowerCase();
			if(!groupManager.existGroup(groupName))
			{
				sender.sendMessage(ChatColor.RED + "Le groupe '" + args[2] + "' n'existe pas.");
				return true;
			}

			if(Bukkit.getWorld(args[3]) == null)
			{
				sender.sendMessage(ChatColor.RED + "Le monde '" + args[3] + "' n'existe pas.");
				return true;
			}
			String worldName = args[3];

			if(groupManager.isWorldInGroup(groupName, worldName))
			{
				sender.sendMessage(ChatColor.RED + "Le monde '" + args[3] + "' est déjà dans le groupe '" + args[2] + "'.");
				return true;
			}

			String finalWorldName = worldName;
			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{
					groupManager.addToGroup(groupName, finalWorldName);
					sender.sendMessage(ChatColor.GREEN + "Le monde '" + args[3] + "' a été ajouté dans le groupe '" + args[2] + "'.");
					return null;
				}
			});

		}
		return true;
	}
}
