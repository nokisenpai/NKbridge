package ovh.lumen.NKbridge.completers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import ovh.lumen.NKbridge.managers.GroupManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RootCompleter implements TabCompleter
{
	List<String> COMMANDS = Arrays.asList("reload", "group");
	List<String> GROUPSUBCOMMANDS = Arrays.asList("create", "delete", "addworld", "removeworld","list");

	public RootCompleter()
	{

	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
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
							org.bukkit.util.StringUtil.copyPartialMatches(args[2], GroupManager.getGroupList(), completions);
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
							org.bukkit.util.StringUtil.copyPartialMatches(args[3], GroupManager.getWorldList(), completions);
							Collections.sort(completions);
							return completions;
						default:
							break;
					}
				default:
					break;
			}

		}

		return null;
	}
}