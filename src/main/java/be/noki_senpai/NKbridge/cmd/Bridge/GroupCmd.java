package be.noki_senpai.NKbridge.cmd.Bridge;

import be.noki_senpai.NKbridge.cmd.Bridge.Group.*;
import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.InventoryManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GroupCmd
{
	GroupManager groupManager = null;
	QueueManager queueManager = null;
	InventoryManager inventoryManager = null;

	public GroupCmd(GroupManager groupManager, QueueManager queueManager, InventoryManager inventoryManager)
	{
		this.groupManager = groupManager;
		this.queueManager = queueManager;
		this.inventoryManager = inventoryManager;
	}

	public boolean group(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasGroupPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		// if no argument
		if(args.length == 1)
		{
			sender.sendMessage(ChatColor.GREEN + "/bridge group <subCommand>");
			return true;
		}

		args[1] = args[1].toLowerCase();
		switch(args[1])
		{
			case "create":
				return new CreateGroup(groupManager, queueManager).createGroup(sender, args);
			case "delete":
				return new DeleteGroup(groupManager, queueManager).deleteGroup(sender, args);
			case "addworld":
				return new AddToGroup(groupManager, queueManager, inventoryManager).addToGroup(sender, args);
			case "removeworld":
				return new RemoveFromGroup(groupManager, queueManager, inventoryManager).removeFromGroup(sender, args);
			case "list":
				return new ListGroup(groupManager).listGroup(sender, args);
			default:
				sender.sendMessage(ChatColor.RED + "Sous-commande inconnue.");
				return true;
		}
	}

	private boolean hasGroupPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkbridge.*") || sender.hasPermission("nkbridge.group")
				|| sender.hasPermission("nkbridge.admin");
	}
}
