package be.noki_senpai.NKbridge.cmd;

import be.noki_senpai.NKbridge.cmd.Bridge.GroupCmd;
import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.InventoryManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BridgeCmd implements CommandExecutor
{
	GroupManager groupManager = null;
	QueueManager queueManager = null;
	InventoryManager inventoryManager = null;

	public BridgeCmd(GroupManager groupManager, QueueManager queueManager, InventoryManager inventoryManager)
	{
		this.groupManager = groupManager;
		this.queueManager = queueManager;
		this.inventoryManager = inventoryManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		// if no argument
		if(args.length == 0)
		{
			sender.sendMessage(ChatColor.GREEN + "/bridge <subCommand>");
			return true;
		}

		args[0] = args[0].toLowerCase();
		switch(args[0])
		{
			case "group":
				return new GroupCmd(groupManager, queueManager, inventoryManager).group(sender, args);
			default:
				sender.sendMessage(ChatColor.RED + "Sous-commande inconnue.");
				return true;
		}
	}
}
