package be.noki_senpai.NKbridge.listeners;

import be.noki_senpai.NKbridge.managers.GroupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BridgeCompleter implements TabCompleter
{
	GroupManager groupManager = null;
	List<String> COMMANDS = Arrays.asList("group");
	List<String> GROUPSUBCOMMANDS = Arrays.asList("create", "delete", "addworld", "removeworld","list");

	public BridgeCompleter(GroupManager groupManager)
	{
		this.groupManager = groupManager;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if(sender instanceof Player)
		{
			if(args.length <= 1)
			{
				final List<String> completions = new ArrayList<>();
				org.bukkit.util.StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
				Collections.sort(completions);
				return completions;
			}
			else if(args.length <= 2)
			{
				switch(args[0].toLowerCase())
				{
					case "group":
						final List<String> completions = new ArrayList<>();
						org.bukkit.util.StringUtil.copyPartialMatches(args[1], GROUPSUBCOMMANDS, completions);
						Collections.sort(completions);
						return completions;
					default:
						break;
				}

			}
			else if(args.length <= 3)
			{
				switch(args[0].toLowerCase())
				{
					case "group":
						switch(args[1].toLowerCase())
						{
							case "delete":
							case "addworld":
							case "removeworld":
							case "list":
								final List<String> completions = new ArrayList<>();
								org.bukkit.util.StringUtil.copyPartialMatches(args[2], groupManager.getGroupList(), completions);
								Collections.sort(completions);
								return completions;
							default:
								break;
						}
					default:
						break;
				}

			}
			else if(args.length <= 4)
			{
				switch(args[0].toLowerCase())
				{
					case "group":
						switch(args[1].toLowerCase())
						{
							case "addworld":
							case "removeworld":
								final List<String> completions = new ArrayList<>();
								org.bukkit.util.StringUtil.copyPartialMatches(args[3], groupManager.getWorldList(), completions);
								Collections.sort(completions);
								return completions;
							default:
								break;
						}
					default:
						break;
				}

			}
		}
		return null;
	}
}