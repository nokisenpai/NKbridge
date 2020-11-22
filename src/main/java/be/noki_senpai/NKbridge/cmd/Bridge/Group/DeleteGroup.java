package be.noki_senpai.NKbridge.cmd.Bridge.Group;

import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import be.noki_senpai.NKbridge.utils.CheckType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.function.Function;

public class DeleteGroup
{
	GroupManager groupManager = null;
	QueueManager queueManager = null;

	public DeleteGroup(GroupManager groupManager, QueueManager queueManager)
	{
		this.groupManager = groupManager;
		this.queueManager = queueManager;
	}

	public boolean deleteGroup(CommandSender sender, String[] args)
	{
		// If no more argument
		if(args.length == 2)
		{
			sender.sendMessage(ChatColor.GREEN + "/bridge group delete <groupName>");
			return true;
		}
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

			if(groupManager.getGroup(groupName).size() > 0)
			{
				sender.sendMessage(
						ChatColor.RED + "Ce groupe est lié à plusieurs monde. Retirez d'abord tous les mondes avant de supprimer ce groupe.");
				return true;
			}

			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{
					groupManager.deleteGroup(groupName);
					sender.sendMessage(ChatColor.GREEN + "Le groupe '" + args[2] + "' a été supprimé.");
					return null;
				}
			});

		}
		return true;
	}
}
