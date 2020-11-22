package be.noki_senpai.NKbridge.cmd.Bridge.Group;

import be.noki_senpai.NKbridge.managers.GroupManager;
import be.noki_senpai.NKbridge.managers.QueueManager;
import be.noki_senpai.NKbridge.utils.CheckType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.function.Function;

public class ListGroup
{
	GroupManager groupManager = null;
	public ListGroup(GroupManager groupManager)
	{
		this.groupManager = groupManager;
	}

	public boolean listGroup(CommandSender sender, String[] args)
	{
		// If no more argument
		if(args.length == 2)
		{
			String msg = ChatColor.GREEN + "Liste des groupes";
			for(String group : groupManager.getGroupList())
			{
				msg += "\n   " + group;
			}
			sender.sendMessage(msg);
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

			String msg = ChatColor.GREEN + "Liste des mondes du groupe " + ChatColor.BLUE + groupName + ChatColor.GREEN;
			for(String world : groupManager.getGroup(groupName))
			{
				msg += "\n   " + world;
			}
			sender.sendMessage(msg);
		}
		return true;
	}
}
